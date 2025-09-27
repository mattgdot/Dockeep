package com.app.dockeep.ui.screens.files.components

import android.net.Uri
import androidx.compose.runtime.Composable
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.ui.components.ConfirmActionDialog
import com.app.dockeep.ui.components.SelectFolderDialog
import com.app.dockeep.ui.components.TextInputDialog

@Composable
fun Dialogs(
    showConfirmDelete: Boolean,
    dismissDelete: () -> Unit,
    onDeleteConfirm: () -> Unit,
    showCreateFolder: Boolean,
    dismissCreateFolder: () -> Unit,
    onCreateFolderConfirm: (String) -> Unit,
    showRename: Boolean,
    dismissRename: () -> Unit,
    onConfirmRename: (String) -> Unit,
    showMove: Boolean,
    dismissMove: () -> Unit,
    onMoveConfirm: (String) -> Unit,
    removeOnMove: Boolean,
    dirList: List<Pair<String, Uri>>,
    rootUri: String,
    effectiveItems: List<DocumentItem>,
    showArchive: Boolean,
    dismissArchive:()->Unit,
    onArchiveConfirm: (String) -> Unit,
) {
    if (showConfirmDelete) {
        ConfirmActionDialog(
            title = "Delete ${effectiveItems.size} item(s)?",
            message = "This action can not be undone.",
            onDismiss = dismissDelete,
            onConfirm = onDeleteConfirm,
        )
    }

    if (showCreateFolder) {
        TextInputDialog(
            title = "Create Folder",
            onDismiss = dismissCreateFolder,
            onConfirm = onCreateFolderConfirm,
        )
    }

    if (showCreateFolder) {
        TextInputDialog(
            title = "Create Folder",
            onDismiss = dismissCreateFolder,
            onConfirm = onCreateFolderConfirm,
        )
    }

    if (showArchive) {
        TextInputDialog(
            title = "Name zip file",
            initialText = "",
            onDismiss = dismissArchive,
            onConfirm = onArchiveConfirm,
        )
    }

    if (showRename) {
        TextInputDialog(
            title = "Rename",
            initialText = effectiveItems[0].name,
            onDismiss = dismissRename,
            onConfirm = onConfirmRename,
        )
    }

    if (showMove) {
        val dirsFiltered = dirList.filterNot { fu ->
            fu.second.toString() == rootUri || effectiveItems.any {
                fu.second.toString().startsWith(it.uri.toString())
            }
        }.toMutableList()

        if (rootUri.isEmpty()) dirsFiltered.removeAt(0)

        SelectFolderDialog(
            title = if (removeOnMove) "Move" else "Copy",
            folders = dirsFiltered,
            onConfirm = onMoveConfirm,
            onDismiss = dismissMove,
        )
    }
}