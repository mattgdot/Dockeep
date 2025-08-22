package com.app.dockeep.data.files

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.utils.Constants.FILES_DIR
import com.app.dockeep.utils.Constants.OCTET_STREAM
import com.app.dockeep.utils.Constants.UNK_FILE
import javax.inject.Inject

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
        folderUri: Uri, files: List<Uri>
    ) {
        val targetDir = DocumentFile.fromTreeUri(context, folderUri)

        for (uri in files) {
            try {
                persistUriPermissions(uri)
            } catch (e: Exception) { }

            var displayName = UNK_FILE
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
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: OCTET_STREAM
            }

            val file = targetDir?.createFile(mimeType, displayName)

            if (file != null) {
                context.contentResolver.openInputStream(uri).use { ifs ->
                    context.contentResolver.openOutputStream(file.uri).use { of ->
                        if (ifs != null && of != null) {
                            ifs.copyTo(of)
                        }
                    }
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
        )

        contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val docIdIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeTypeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                val docId = cursor.getString(docIdIndex)
                val displayName = cursor.getString(nameIndex)
                val mimeType = cursor.getString(mimeTypeIndex)

                val docUri = DocumentsContract.buildDocumentUriUsingTree(
                    targetDir.uri, docId
                )

                files.add(
                    DocumentItem(
                        id = docId,
                        name = displayName,
                        mimeType = mimeType,
                        isFolder = mimeType == DocumentsContract.Document.MIME_TYPE_DIR,
                        uri = docUri
                    )
                )
            }
        }
        return files
    }

    override suspend fun createDirectory(parentUri: Uri, folderName: String): Uri {
        val targetDir = DocumentFile.fromTreeUri(context, parentUri)

        val dir = targetDir?.findFile(folderName)
        if (dir == null) targetDir?.createDirectory(folderName)

        return if (targetDir != null) targetDir.findFile(folderName)?.uri
            ?: Uri.EMPTY else Uri.EMPTY
    }

    override suspend fun listAllDirectories(root: Uri): List<Pair<String, Uri>> {
        val targetDir = DocumentFile.fromTreeUri(context, root)
        val list = mutableListOf<Pair<String,Uri>>()

        targetDir?.let{
            for(file in it.listFiles()) {
                if(file.isDirectory) {
                    list.add(Pair(file.name!!, file.uri, ))
                }
            }
        }

        return list.toList()
    }
}