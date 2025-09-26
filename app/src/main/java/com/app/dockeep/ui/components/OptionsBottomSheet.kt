package com.app.dockeep.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.Compress
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
    items: List<DocumentItem>,
    onDismiss: () -> Unit,
    onOpen: (DocumentItem) -> Unit,
    onShare: (List<DocumentItem>) -> Unit,
    onDelete: (List<DocumentItem>) -> Unit,
    onRename: (DocumentItem) -> Unit,
    onMove: (List<DocumentItem>, Boolean) -> Unit,
    onCompress:(List<DocumentItem>) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column {
            if (items.size == 1) {
                if (!items[0].isFolder) {
                    OptionsListItem("Open", Icons.Outlined.OpenWith) {
                        onOpen(items[0])
                        onDismiss()
                    }
                }
                OptionsListItem("Rename", Icons.Outlined.Edit) {
                    onRename(items[0])
                    onDismiss()
                }
            }
            if (!items.any { it.isFolder }) {
                OptionsListItem("Share", Icons.Outlined.Share) {
                    onShare(items)
                    onDismiss()
                }
            }

            OptionsListItem("Move to", Icons.AutoMirrored.Outlined.ArrowForward) {
                onMove(items, true)
                onDismiss()
            }
            OptionsListItem("Copy to", Icons.Default.ContentCopy) {
                onMove(items, false)
                onDismiss()
            }
            OptionsListItem("Delete", Icons.Outlined.Delete) {
                onDelete(items)
                onDismiss()
            }
            OptionsListItem("Compress", Icons.Outlined.Compress) {
                onCompress(items)
                onDismiss()
            }
        }
    }
}