package com.app.dockeep.data.files

import android.net.Uri
import com.app.dockeep.model.DocumentItem

interface FilesRepository {
    suspend fun persistUriPermissions(uri: Uri)
    suspend fun copyFilesToFolder(folderUri: Uri, files: List<Uri>, isRoot: Boolean)
    suspend fun listFilesInDirectory(folderUri: Uri, isRoot: Boolean): List<DocumentItem>
}