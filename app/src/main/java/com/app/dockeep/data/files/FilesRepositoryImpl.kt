package com.app.dockeep.data.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.database.getLongOrNull
import androidx.documentfile.provider.DocumentFile
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.utils.Constants.FILES_DIR
import com.app.dockeep.utils.Constants.OCTET_STREAM
import com.app.dockeep.utils.Constants.UNK
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.collections.plusAssign

class FilesRepositoryImpl @Inject constructor(
    private val context: Context
) : FilesRepository {

    override suspend fun persistUriPermissions(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    override suspend fun setRootLocation(uri: Uri): Uri {
        persistUriPermissions(uri)
        val rootUri = createDirectory(uri, FILES_DIR)
        return rootUri
    }

    override suspend fun copyFilesToFolder(
        folderUri: Uri, files: List<Uri>, delete: Boolean
    ) {
        val targetDir = DocumentFile.fromTreeUri(context, folderUri) ?: return

        for (uri in files) {
            val target = DocumentFile.fromSingleUri(context, uri)
            if (target?.isDirectory == false) {
                try {
                    persistUriPermissions(uri)
                } catch (e: Exception) {
                    Log.e("FilesRepository", "Failed to persist URI permission for $uri", e)
                }

                var displayName = UNK
                var mimeType: String?

                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = cursor.getString(nameIndex)
                        }
                    }
                }

                mimeType = context.contentResolver.getType(uri)

                if (mimeType == null) {
                    val extension = displayName.substringAfterLast('.', "")
                    mimeType =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                            ?: OCTET_STREAM
                }

                val file = targetDir.createFile(mimeType, displayName)

                if (file != null) {
                    context.contentResolver.openInputStream(uri).use { ifs ->
                        context.contentResolver.openOutputStream(file.uri).use { of ->
                            if (ifs != null && of != null) {
                                ifs.copyTo(of)
                            }
                        }
                    }
                    if (delete) {
                        try {
                            deleteDocument(uri)
                        } catch (_: Exception) {

                        }
                    }
                }
            } else {
                target?.let { iuri ->
                    val nuri = createDirectory(folderUri, iuri.name!!)
                    val files = listFilesInDirectory(iuri.uri)

                    copyFilesToFolder(nuri, files.map { it.uri })
                }
            }
        }
    }

    override suspend fun listFilesInDirectory(folderUri: Uri): List<DocumentItem> {

        val files = mutableListOf<DocumentItem>()

        val targetDir = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyList()

        val contentResolver = context.contentResolver

        val targetDocId = DocumentsContract.getDocumentId(targetDir.uri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            targetDir.uri, targetDocId
        )

        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        if (!pathExists(targetDir.uri)) return emptyList()

        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val docIdIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val size = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)
            val date = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(docIdIndex)
                val displayName = cursor.getString(nameIndex)
                val mimeType = cursor.getString(mimeTypeIndex)
                val size = cursor.getLongOrNull(size)
                val date = cursor.getLongOrNull(date)

                val docUri = DocumentsContract.buildDocumentUriUsingTree(
                    targetDir.uri, docId
                )

                files.add(
                    DocumentItem(
                        id = docId,
                        name = displayName,
                        mimeType = mimeType,
                        isFolder = mimeType == DocumentsContract.Document.MIME_TYPE_DIR,
                        uri = docUri,
                        size = size,
                        date = date
                    )
                )
            }
        }

        return files
    }

    override suspend fun createDirectory(parentUri: Uri, folderName: String): Uri {
        val targetDir = DocumentFile.fromTreeUri(context, parentUri) ?: return Uri.EMPTY
        val dir = targetDir.findFile(folderName) ?: targetDir.createDirectory(folderName)
        return dir?.uri ?: Uri.EMPTY
    }

    override suspend fun listAllDirectories(root: Uri, parent: String): List<Pair<String, Uri>> {
        val targetDir = DocumentFile.fromTreeUri(context, root) ?: return emptyList()
        val result = mutableListOf<Pair<String, Uri>>()

        val fileName =
            if (parent.isEmpty()) targetDir.name ?: UNK else "$parent/${targetDir.name ?: UNK}"

        if (targetDir.isDirectory) {
            result.add(fileName to root)
        }

        targetDir.listFiles()
            .filter { it.isDirectory }
            .forEach { dir ->
                result += listAllDirectories(dir.uri, fileName)
            }

        return result
    }

    override suspend fun listAllFiles(
        dir: DocumentFile,
        parent: String
    ): List<Pair<String, Uri>> {
        val result = mutableListOf<Pair<String, Uri>>()

        val fileName = if (parent.isEmpty()) dir.name ?: UNK else "$parent/${dir.name ?: UNK}"

        if (dir.isDirectory) {
            result.add("$fileName/" to dir.uri)
            dir.listFiles().forEach { child ->
                result += listAllFiles(child, fileName)
            }
        } else if (dir.isFile) {
            result.add(fileName to dir.uri)
        }

        return result
    }


    override suspend fun renameDocument(uri: Uri, name: String) {
        DocumentsContract.renameDocument(context.contentResolver, uri, name)
    }

    override suspend fun deleteDocument(uri: Uri) {
        try {
            persistUriPermissions(uri)
        } catch (e: Exception) {
            Log.e("FilesRepository", "Failed to delete document $uri", e)
        }
        val target = DocumentFile.fromSingleUri(context, uri)
        target?.delete()
    }

    override suspend fun pathExists(uri: Uri): Boolean {
        return DocumentFile.fromTreeUri(context, uri)?.exists() == true
    }

    override suspend fun moveDocument(uri: Uri, destination: Uri) {

    }

    override suspend fun searchFiles(query: String, root: Uri): List<DocumentItem> {
        val targetDir = DocumentFile.fromTreeUri(context, root) ?: return emptyList()
        val result = mutableListOf<DocumentItem>()

        val files = listFilesInDirectory(targetDir.uri)

        files.forEach {
            if (it.isFolder) {
                result += searchFiles(query, it.uri)
            } else if (it.name.lowercase().contains(query.lowercase())) {
                result += it
            }
        }

        return result
    }

    override suspend fun createArchive(
        root: Uri,
        name: String,
        files: List<Uri>
    ) {
        val inputDirectory = DocumentFile.fromTreeUri(context, root) ?: return

        val outputZipFile = inputDirectory.createFile("application/zip", name)

        val fileList = mutableListOf<Pair<String, Uri>>()
        files.forEach {
            val item = DocumentFile.fromTreeUri(context, it)!!
            if(item.isDirectory) fileList += listAllFiles(item)
            else fileList.add(item.name!! to item.uri)
        }

        println(fileList)

        if (outputZipFile != null) {
            context.contentResolver.openOutputStream(outputZipFile.uri).use { of ->
                if (of != null) {
                    ZipOutputStream(BufferedOutputStream(of)).use { zos ->
                        fileList.forEach { file ->
                            val zipFileName = file.first
                            val entry = ZipEntry(zipFileName)
                            zos.putNextEntry(entry)
                            val inpFile = DocumentFile.fromSingleUri(context, file.second)
                            inpFile?.isDirectory?.let {
                                if (!it) {
                                    context.contentResolver.openInputStream(inpFile.uri)
                                        .use { ifs ->
                                            ifs?.copyTo(zos)
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}