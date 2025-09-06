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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.collections.getOrNull

@Composable
fun SelectFolderDialog(
    title: String,
    folders: List<Pair<String, Uri>>,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

    AlertDialog(
        icon = {
            Icon(Icons.Default.Add, contentDescription = null)
        },
        title = {
            Text(text = title)
        },
        text = {
            LargeMenuDropdown(
                label = "Folder",
                items = folders,
                selectedIndex = selectedIndex,
                selectedItemToString = { it.first },
                onItemSelected = { index, _ -> selectedIndex = index },
                modifier = Modifier.padding(10.dp)
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(folders.getOrNull(selectedIndex)?.second?.toString() ?: "")
            }) {
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