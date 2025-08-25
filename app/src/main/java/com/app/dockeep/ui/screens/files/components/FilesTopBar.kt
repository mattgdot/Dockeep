package com.app.dockeep.ui.screens.files.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesTopBar(title: String, displayBackIcon: Boolean, scrollBehaviour: TopAppBarScrollBehavior, onCreateFolder:()->Unit, onGoBack: () -> Unit,) {

    TopAppBar(
        scrollBehavior = scrollBehaviour,
        title = {
            Text(title)
        },
        actions = {
            IconButton(
                onClick = onCreateFolder
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreateNewFolder, contentDescription = null
                )
            }

            IconButton(
                onClick = {
                    // open settings
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings, contentDescription = null
                )
            }
        },
        navigationIcon = {
            if (displayBackIcon) {
                IconButton(onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null
                    )
                }
            }
        },
    )
}