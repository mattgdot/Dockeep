package com.app.dockeep.ui.screens.files

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.ui.MainViewModel
import com.app.dockeep.ui.components.ConfirmActionDialog
import com.app.dockeep.ui.components.OptionsBottomSheet
import com.app.dockeep.ui.components.TextInputDialog
import com.app.dockeep.ui.screens.files.components.FileListItem
import com.app.dockeep.ui.screens.files.components.FilesEmptyState
import com.app.dockeep.ui.screens.files.components.FilesFAB
import com.app.dockeep.ui.screens.files.components.FilesTopBar
import com.app.dockeep.utils.Helper.openDocument
import com.app.dockeep.utils.Helper.openDocumentIntent
import com.app.dockeep.utils.Helper.shareDocument

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

    val context = LocalContext.current
    val scrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    var showCreateFolderDialog by rememberSaveable { mutableStateOf(false) }
    var showRenameFileDialog by rememberSaveable { mutableStateOf(false) }
    var showOptionsSheet by rememberSaveable { mutableStateOf(false) }
    var showConfirmDelete by rememberSaveable { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<DocumentItem?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val filesList by mainVM.files

    LaunchedEffect(key1 = Unit) {
        if (mainVM.getContentPathUri().isNullOrBlank()) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            contentPathLauncher.launch(intent)
        } else {
            mainVM.loadFiles(uri)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        floatingActionButton = {
            FilesFAB {
                filePathLauncher.launch(openDocumentIntent)
            }
        },
        topBar = {
            FilesTopBar(
                title = path,
                displayBackIcon = navController.previousBackStackEntry != null,
                scrollBehaviour = scrollBehaviour,
                onCreateFolder = {
                    showCreateFolderDialog = true
                },
            ) {
                navController.popBackStack()
            }
        },

        ) { innerPadding ->

        BackHandler(navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }

        LaunchedEffect(lifecycleState) {
            if (lifecycleState == Lifecycle.State.RESUMED) mainVM.loadFiles(uri)
        }

        if(showConfirmDelete) {
            selectedItem?.let{
                ConfirmActionDialog(
                    title = "Delete permanently?",
                    message = "This action can not be undone.",
                    onDismiss = { showConfirmDelete = false },
                    onConfirm = {
                        mainVM.deleteFile(uri, it.uri)
                        showConfirmDelete = false
                    }
                )
            }
        }

        if (showCreateFolderDialog) {
            TextInputDialog(
                title = "Create Folder",
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

        if (showRenameFileDialog) {
            TextInputDialog(
                title = "Rename",
                initialText = selectedItem?.name ?: "",
                onDismiss = {
                    showRenameFileDialog = false
                },
                onConfirm = {
                    mainVM.renameFile(
                        uri, selectedItem?.uri!!, it
                    )
                    showRenameFileDialog = false
                },
            )
        }

        if (showOptionsSheet) {
            selectedItem?.let { item ->
                OptionsBottomSheet(
                    item = item,
                    onDismiss = {
                        showOptionsSheet = false
                    },
                    onRename = {
                        showRenameFileDialog = true
                        showOptionsSheet = false
                    },
                    onDelete = {
                        showConfirmDelete = true
                        showOptionsSheet = false
                    },
                    onOpen = {
                        context.openDocument(item.uri, item.mimeType)
                        showOptionsSheet = false
                    },
                    onShare = {
                        context.shareDocument(item.uri, item.mimeType)
                        showOptionsSheet = false
                    },
                )
            }
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            if (filesList.isEmpty()) {
                FilesEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filesList) { item ->
                        FileListItem(
                            item = item,
                            onClick = {
                                if (item.isFolder) onNavigate(item.uri.toString(), item.name)
                                else context.openDocument(item.uri, item.mimeType)
                            },
                            onMoreClick = {
                                selectedItem = item
                                showOptionsSheet = true
                            },
                        )
                    }
                }
            }
        }
    }
}