package com.app.dockeep.ui.screens.files

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesTopBar(title: String, displayBackIcon: Boolean, onGoBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(title)
        },
        actions = {
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