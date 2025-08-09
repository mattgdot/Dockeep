package com.app.dockeep.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.dockeep.data.preferences.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {
    fun getContentPathUri():String? = runBlocking { repository.getString("CONTENT_PATH_URI") }

    fun setContentPathUri(uri: String) {
        viewModelScope.launch {
            repository.putString("CONTENT_PATH_URI", uri)
        }
    }
}