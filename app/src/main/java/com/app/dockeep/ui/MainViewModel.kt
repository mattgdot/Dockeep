package com.app.dockeep.ui

import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.dockeep.data.files.FilesRepository
import com.app.dockeep.data.preferences.DataStoreRepository
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.utils.Constants.CONTENT_PATH_KEY
import com.app.dockeep.utils.Constants.SORT_TYPE_KEY
import com.app.dockeep.utils.Constants.THEME_KEY
import com.app.dockeep.utils.Helper.extractUris
import com.app.dockeep.utils.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.sortedWith

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefRepo: DataStoreRepository,
    private val filesRepo: FilesRepository,
    application: Application
) : AndroidViewModel(application) {

    var files = mutableStateOf(listOf<DocumentItem>())
    var folders = mutableStateOf(listOf<Pair<String, Uri>>())
    val loading = mutableStateOf(false)
    var launched = false
    //var theme = mutableStateOf(ThemeMode.AUTO)
    private val _theme = MutableStateFlow(ThemeMode.AUTO) // Valoare inițială
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    init {
        println("init")
        getAppTheme()
        viewModelScope.launch(Dispatchers.IO) {
            getContentPathUri()?.let { uri ->
                loadFiles(uri)
            }
        }
    }

    suspend fun getContentPathUri(): String? = prefRepo.getString(CONTENT_PATH_KEY)

    fun getSortType(): String = runBlocking { prefRepo.getString(SORT_TYPE_KEY) ?: "Name A - Z" }

    fun setSortType(type: String) {
        viewModelScope.launch {
            prefRepo.putString(SORT_TYPE_KEY, type)
        }
    }

    fun getAppTheme() = runBlocking{ prefRepo.getString(THEME_KEY)?.let { _theme.value= ThemeMode.valueOf(it) } ?: ThemeMode.AUTO }

    fun setAppTheme(th: ThemeMode) {
        viewModelScope.launch {
            _theme.value = th
            prefRepo.putString(THEME_KEY, th.name)
        }
    }

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
        launched = true
        viewModelScope.launch {
            filesRepo.setRootLocation(uri).toString().let {
                prefRepo.putString(CONTENT_PATH_KEY, it)
                loadFiles(getContentPathUri() ?: "")
            }
        }
    }

    suspend fun loadFiles(folderUri: String = "") {
        if (files.value.isEmpty()) setLoading(true)

        val folder = resolveFolderUri(folderUri)

        val fileList = withContext(Dispatchers.IO) {
            filesRepo.listFilesInDirectory(folder)
        }

        withContext(Dispatchers.Main) {
            files.value = fileList
            sortFiles(getSortType())
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

    fun searchFiles(query: String, parent: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                val result = withContext(Dispatchers.IO) {
                    filesRepo.searchFiles(
                        query, getContentPathUri()?.toUri() ?: Uri.EMPTY
                    )
                }
                withContext(Dispatchers.Main) {
                    files.value = result
                }

            } else {
                loadFiles(parent)
            }

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

    fun sortFiles(type: String) {

        setSortType(type)

        when (type) {
            "Name A - Z" -> {
                files.value =
                    files.value.sortedWith(compareByDescending<DocumentItem> { it.isFolder }.thenBy { if (!it.isFolder) it.name.first() else "" })
            }

            "Name Z - A" -> {
                files.value =
                    files.value.sortedWith(compareByDescending<DocumentItem> { it.isFolder }.thenByDescending { if (!it.isFolder) it.name.first() else "" })
            }

            "Largest first" -> {
                files.value =
                    files.value.sortedWith(compareByDescending<DocumentItem> { it.isFolder }.thenByDescending { if (!it.isFolder) it.size else 0L })
            }

            "Smallest first" -> {
                files.value =
                    files.value.sortedWith(compareByDescending<DocumentItem> { it.isFolder }.thenBy { if (!it.isFolder) it.size else Long.MAX_VALUE })
            }

            "Newest first" -> {
                files.value =
                    files.value.sortedWith(compareByDescending<DocumentItem> { it.isFolder }.thenByDescending { if (!it.isFolder) it.date else Long.MIN_VALUE })
            }

            "Oldest first" -> {
                files.value =
                    files.value.sortedWith(compareByDescending<DocumentItem> { it.isFolder }.thenBy { if (!it.isFolder) it.date else Long.MAX_VALUE })
            }
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
            if (remove) loadFiles(src.toString())
        }
    }
}