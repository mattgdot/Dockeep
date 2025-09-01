package com.app.dockeep.ui

import android.app.Activity
import android.app.Application
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefRepo: DataStoreRepository,
    private val filesRepo: FilesRepository,
    application: Application
) : AndroidViewModel(application) {

    var files = mutableStateOf(listOf<DocumentItem>())
    var folders = mutableStateOf(listOf<Pair<String, Uri>>())
    val loading = mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getContentPathUri()?.let { uri ->
                loadFiles(uri)
            }
        }
    }

    suspend fun getContentPathUri(): String? = prefRepo.getString(CONTENT_PATH_KEY)

    suspend fun rootExists(): Boolean =
        filesRepo.pathExists(getContentPathUri()?.toUri() ?: Uri.EMPTY)

    private suspend fun resolveFolderUri(folderUri: String): Uri {
        val uriString = folderUri.ifBlank { getContentPathUri() ?: "" }
        return uriString.toUri()
    }

    private suspend fun setLoading(boolean: Boolean) {
        withContext(Dispatchers.Main) {
            loading.value = boolean
        }
    }

    fun setContentPathUri(result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) return
        val uri = result.data?.data ?: return

        viewModelScope.launch {
            filesRepo.setRootLocation(uri).toString().let {
                prefRepo.putString(CONTENT_PATH_KEY, it)
                loadFiles(it)
            }
        }
    }

    suspend fun loadFiles(folderUri: String = "") {
        if(files.value.isEmpty()) setLoading(true)

        val folder = resolveFolderUri(folderUri)

        val fileList = withContext(Dispatchers.IO) {
            filesRepo.listFilesInDirectory(folder)
                .sortedWith(compareByDescending<DocumentItem> { it.isFolder }.thenBy { it.name })
        }

        withContext(Dispatchers.Main) {
            files.value = fileList
        }

        setLoading(false)

        val folderList = withContext(Dispatchers.IO) {
            filesRepo.listAllDirectories(getContentPathUri()?.toUri() ?: Uri.EMPTY)
        }

        withContext(Dispatchers.Main) {
            folders.value = folderList
        }
    }


    fun loadAndCopyFiles(folderUri: String = "", result: ActivityResult) {
        if (result.resultCode != Activity.RESULT_OK) return

        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val uris = result.data?.extractUris() ?: return@launch
            val folder = resolveFolderUri(folderUri)
            filesRepo.copyFilesToFolder(folder, uris)
            loadFiles(folder.toString())
        }
    }

    fun createFolder(parent: String = "", name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val folder = resolveFolderUri(parent)
            filesRepo.createDirectory(folder, name)
            loadFiles(folder.toString())
        }
    }

    fun deleteFiles(folder: String, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            uris.forEach { doc ->
                filesRepo.deleteDocument(doc)
            }
            loadFiles(folder)
        }
    }

    fun renameFile(folder: String = "", doc: Uri, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            filesRepo.renameDocument(doc, name)
            loadFiles(folder)
        }
    }

    fun importFiles(
        uris: List<Uri>,
        folderUri: String = "",
        load: Boolean = true,
        remove: Boolean = false,
        src: String = ""
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            setLoading(true)
            val folder = resolveFolderUri(folderUri)

            filesRepo.copyFilesToFolder(folder, uris)
            if (remove) uris.forEach { filesRepo.deleteDocument(it) }

            val src = resolveFolderUri(src)

            setLoading(false)

            if (load) loadFiles(folderUri)
            if(remove) loadFiles(src.toString())
        }
    }
}