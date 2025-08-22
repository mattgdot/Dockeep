package com.app.dockeep.data.files

import android.net.Uri
import com.app.dockeep.model.DocumentItem

interface FilesRepository {
    suspend fun persistUriPermissions(uri: Uri)
    suspend fun setRootLocation(uri: Uri): Uri
    suspend fun copyFilesToFolder(folderUri: Uri, files: List<Uri>)
    suspend fun listFilesInDirectory(folderUri: Uri): List<DocumentItem>
    suspend fun createDirectory(parentUri: Uri, folderName: String): Uri
    suspend fun listAllDirectories(root: Uri): List<Pair<String, Uri>>
}