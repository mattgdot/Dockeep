package com.app.dockeep.ui.screens.files

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit, onConfirm: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        icon = {
            Icon(Icons.Default.Add, contentDescription = null)
        },
        title = {
            Text(text = "Create Folder")
        },
        text = {
            OutlinedTextField(
                value = text,
                label = { Text("Name") },
                onValueChange = { text = it },
                modifier = Modifier.padding(5.dp).focusRequester(focusRequester),
            )
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(text)
                }) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }) {
                Text("Cancel")
            }
        },
    )
}