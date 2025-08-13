package com.app.dockeep.model

import android.net.Uri

data class DocumentItem(
    val id: String,
    val name: String,
    val mimeType: String,
    val isFolder: Boolean,
    val uri: Uri
)