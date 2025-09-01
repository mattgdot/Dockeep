package com.app.dockeep.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoadingDialog() {
    AlertDialog(
        title = {
            Text(text = "One moment...")
        },
        text = {
            LinearProgressIndicator()
        },
        onDismissRequest = {},
        confirmButton = {
        },
        dismissButton = {
        },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    )
}