package com.app.dockeep.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.dockeep.data.files.FilesRepository
import com.app.dockeep.data.preferences.DataStoreRepository
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.utils.Constants.CONTENT_PATH_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefRepo: DataStoreRepository,
    private val filesRepo: FilesRepository,
) : ViewModel() {

    var files = mutableStateOf(listOf<DocumentItem>())

    val openFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        setType("*/*")
    }

    fun getContentPathUri(): String? = runBlocking { prefRepo.getString(CONTENT_PATH_KEY) }

    fun setContentPathUri(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                viewModelScope.launch {
                    prefRepo.putString(CONTENT_PATH_KEY, uri.toString())
                    filesRepo.persistUriPermissions(uri)
                    loadFiles(uri.toString())
                }
            }
        }
    }

    fun loadFiles(uri: String) {
        viewModelScope.launch {
            var isRoot = false
            val folderUri = uri.ifBlank {
                isRoot = true
                getContentPathUri()!!
            }.toUri()
            files.value = filesRepo.listFilesInDirectory(folderUri, isRoot).toList()
        }
    }

    fun loadAndCopyFiles(folderUri: String, result: ActivityResult) {
        viewModelScope.launch {
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val uriList = mutableListOf<Uri>()
                    var isRoot = false

                    for (i in 0..((it.clipData?.itemCount?.minus(1)) ?: 0)) {
                        it.clipData?.getItemAt(i)?.uri?.let { uri -> uriList.add(uri) }
                        it.data?.let { uri -> uriList.add(uri) }
                    }

                    val dir = folderUri.ifBlank {
                        isRoot = true
                        getContentPathUri()!!
                    }

                    filesRepo.copyFilesToFolder(dir.toUri(), uriList, isRoot)

                    loadFiles(dir)
                }
            }
        }
    }
}