package com.app.dockeep.ui.screens.files

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.app.dockeep.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    mainVM: MainViewModel = hiltViewModel(),
    path: String,
    uri: String,
    navController: NavController,
    onNavigate: (uri: String, path: String) -> Unit
) {
    val contentPathLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            mainVM.setContentPathUri(result)
        }

    val filePathLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            mainVM.loadAndCopyFiles(uri, result)
        }

    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showCreateFolderDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        floatingActionButton = {
            FilesFAB {
                filePathLauncher.launch(mainVM.openFileIntent)
            }
        },
        topBar = {
            FilesTopBar(
                title = path,
                displayBackIcon = navController.previousBackStackEntry != null,
                scrollBehaviour = scrollBehaviour,
                onCreateFolder = {
                    showCreateFolderDialog = true
                }) {
                navController.popBackStack()
            }
        },

        ) { innerPadding ->

        val filesList by mainVM.files

        BackHandler(navController.previousBackStackEntry != null ) {
            navController.popBackStack()
            println("back")
        }

        LaunchedEffect(key1 = Unit) {
            if (mainVM.getContentPathUri().isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                contentPathLauncher.launch(intent)
            } else {
                mainVM.loadFiles(uri)
            }
        }

        if (showCreateFolderDialog) {
            CreateFolderDialog(
                onDismiss = {
                    showCreateFolderDialog = false
                },
                onConfirm = {
                    mainVM.createFolder(
                        uri, it
                    )
                    showCreateFolderDialog = false
                },
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(), contentPadding = innerPadding
        ) {
            items(filesList) { item ->
                ListItem(
                    headlineContent = {
                        Text(
                            item.name,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            fontWeight = if (item.isFolder) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    supportingContent = {
                        Text(
                            if (item.isFolder) "Folder" else item.mimeType,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    },
                    trailingContent = {
                        IconButton(
                            onClick = {}) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.isFolder) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.InsertDriveFile,
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .clickable {
                            if (item.isFolder) {
                                onNavigate(item.uri.toString(), item.name)
                            }
                        },
                )
            }
        }
    }
}
