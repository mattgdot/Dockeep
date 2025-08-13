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

    override suspend fun copyFilesToFolder(
        folderUri: Uri, files: List<Uri>, isRoot: Boolean
    ) {
        val targetDir = if(isRoot) DocumentFile.fromTreeUri(context, folderUri)?.findFile(FILES_DIR) else DocumentFile.fromTreeUri(context, folderUri)

        for (uri in files) {
            persistUriPermissions(uri)

            var displayName = UNK_FILE
            var mimeType: String? = null

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
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: OCTET_STREAM
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

    override suspend fun listFilesInDirectory(folderUri: Uri, isRoot: Boolean): List<DocumentItem> {
        val files = mutableListOf<DocumentItem>()

        var targetDir = DocumentFile.fromTreeUri(context, folderUri)

        if (isRoot) {
            val dir = targetDir?.findFile(FILES_DIR)
            if(dir == null) targetDir?.createDirectory(FILES_DIR)
            targetDir = DocumentFile.fromTreeUri(context, folderUri)?.findFile(FILES_DIR)
        } else {
            targetDir = DocumentFile.fromTreeUri(context, folderUri) ?: return emptyList()
        }

        val contentResolver = context.contentResolver

        val targetDocId = DocumentsContract.getDocumentId(targetDir?.uri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            targetDir?.uri, targetDocId
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
                    targetDir?.uri, docId
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
}