package com.app.dockeep.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.lang.Exception

object Helper {
    fun Context.openDocument(uri: Uri, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Can't open document", Toast.LENGTH_SHORT).show()
        }
    }

    fun Context.shareDocument(uri: Uri, mimeType: String?) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, null))
        } catch (_: Exception) {
            Toast.makeText(this, "Can't share document", Toast.LENGTH_SHORT).show()
        }
    }

    fun Intent.extractUris(): List<Uri> {
        val uriList = mutableListOf<Uri>()
        clipData?.let { clip ->
            for (i in 0 until clip.itemCount) {
                clip.getItemAt(i).uri?.let { uriList.add(it) }
            }
        }
        data?.let { uriList.add(it) }
        return uriList
    }

    val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        setType("*/*")
    }
}