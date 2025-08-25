package com.app.dockeep.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShareImportDialog(
    folders: List<Pair<String, Uri>>,
    selectedIndex: Int,
    onFolderSelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Add, contentDescription = null)
        },
        title = {
            Text(text = "Import file(s)")
        },
        text = {
            LargeMenuDropdown(
                label = "Folder",
                items = folders,
                selectedIndex = selectedIndex,
                selectedItemToString = { it.first },
                onItemSelected = { index, _ -> onFolderSelected(index) },
                modifier = Modifier.padding(10.dp)
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}