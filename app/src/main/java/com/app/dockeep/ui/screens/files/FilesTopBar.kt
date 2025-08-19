package com.app.dockeep.ui.screens.files

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesTopBar(title: String, displayBackIcon: Boolean, scrollBehaviour: TopAppBarScrollBehavior, onGoBack: () -> Unit) {

    TopAppBar(
        scrollBehavior = scrollBehaviour,
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