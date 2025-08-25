package com.app.dockeep.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.dockeep.data.files.FilesRepository
import com.app.dockeep.data.preferences.DataStoreRepository
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.utils.Constants.CONTENT_PATH_KEY
import com.app.dockeep.utils.Helper.extractUris
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefRepo: DataStoreRepository,
    private val filesRepo: FilesRepository,
    application: Application
) : AndroidViewModel(application) {

    var files = mutableStateOf(listOf<DocumentItem>())
    var folders = mutableStateOf(listOf<Pair<String, Uri>>())

    init {
        viewModelScope.launch {
            getContentPathUri()?.let { uri ->
                folders.value = filesRepo.listAllDirectories(uri.toUri())
            }
        }
    }

    suspend fun getContentPathUri(): String? = prefRepo.getString(CONTENT_PATH_KEY)

    private suspend fun resolveFolderUri(folderUri: String): Uri {
        val uriString = folderUri.ifBlank {
            getContentPathUri() ?: ""
        }
        return uriString.toUri()
    }

    fun setContentPathUri(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) return
        val uri = result.data?.data ?: return

        viewModelScope.launch {
            val rootUri = filesRepo.setRootLocation(uri)
            prefRepo.putString(CONTENT_PATH_KEY, rootUri.toString())
            loadFiles(rootUri.toString())
        }
    }

    fun loadFiles(folderUri: String = "") {
        viewModelScope.launch {
            val folder = resolveFolderUri(folderUri)
            files.value = filesRepo.listFilesInDirectory(folder)
                .sortedWith(compareByDescending<DocumentItem> { it.isFolder }.thenBy { it.name })
        }
    }

    fun loadAndCopyFiles(folderUri: String = "", result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) return

        viewModelScope.launch {
            val uris = result.data?.extractUris() ?: return@launch
            val folder = resolveFolderUri(folderUri)
            filesRepo.copyFilesToFolder(folder, uris)
            loadFiles(folder.toString())
        }
    }

    fun createFolder(parent: String = "", name: String) {
        viewModelScope.launch {
            val folder = resolveFolderUri(parent)
            filesRepo.createDirectory(folder, name)
            loadFiles(folder.toString())
        }
    }

    fun deleteFile(folder: String = "", doc: Uri) {
        viewModelScope.launch {
            filesRepo.deleteDocument(doc)
            loadFiles(folder)
        }
    }

    fun renameFile(folder: String = "", doc: Uri, name: String) {
        viewModelScope.launch {
            filesRepo.renameDocument(doc, name)
            loadFiles(folder)
        }
    }

    fun importFiles(intent: Intent, folderUri: String = "") {
        viewModelScope.launch {
            val uris = intent.extractUris()
            val folder = resolveFolderUri(folderUri)
            filesRepo.copyFilesToFolder(folder, uris)
            Toast.makeText(getApplication(), "Imported successfully", Toast.LENGTH_SHORT).show()
            loadFiles(folder.toString())
        }
    }
}