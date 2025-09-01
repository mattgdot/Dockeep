package com.app.dockeep.ui.screens.files.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.app.dockeep.model.DocumentItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(
    selectedItems: SnapshotStateList<DocumentItem>,
    resetSelectionMode: () -> Unit,
    openOptionsClick: ()->Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "${selectedItems.size} selected",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        navigationIcon = {
            IconButton(
                onClick = resetSelectionMode,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        actions = {
            IconButton(
                onClick = openOptionsClick,
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}