package com.app.dockeep.data.files

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.app.dockeep.model.DocumentItem

interface FilesRepository {
    suspend fun persistUriPermissions(uri: Uri)
    suspend fun setRootLocation(uri: Uri): Uri
    suspend fun copyFilesToFolder(folderUri: Uri, files: List<Uri>, delete:Boolean=false)
    suspend fun listFilesInDirectory(folderUri: Uri): List<DocumentItem>
    suspend fun createDirectory(parentUri: Uri, folderName: String): Uri
    suspend fun listAllDirectories(root: Uri, parent:String=""): List<Pair<String, Uri>>
    suspend fun listAllFiles(dir:DocumentFile, parent:String=""): List<Pair<String, Uri>>
    suspend fun renameDocument(uri: Uri, name: String)
    suspend fun deleteDocument(uri: Uri)
    suspend fun pathExists(uri: Uri): Boolean
    suspend fun moveDocument(uri: Uri, destination: Uri)
    suspend fun searchFiles(query: String, root: Uri): List<DocumentItem>
    suspend fun createArchive(root:Uri, name: String, files:List<Uri>)
}