package com.app.dockeep.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.OpenWith
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import com.app.dockeep.model.DocumentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsBottomSheet(
    item: DocumentItem,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column {
            if (!item.isFolder) {
                OptionsListItem("Open", Icons.Outlined.OpenWith, onOpen)
                OptionsListItem("Share", Icons.Outlined.Share, onShare)
            }
            OptionsListItem("Rename", Icons.Outlined.Edit, onRename)
            OptionsListItem("Delete", Icons.Outlined.Delete, onDelete)
        }
    }

}