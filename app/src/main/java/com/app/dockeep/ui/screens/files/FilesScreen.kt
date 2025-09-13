package com.app.dockeep.ui.screens.files

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.ui.MainViewModel
import com.app.dockeep.ui.components.AppSearchBar
import com.app.dockeep.ui.components.LoadingDialog
import com.app.dockeep.ui.components.OptionsBottomSheet
import com.app.dockeep.ui.components.SortBottomSheet
import com.app.dockeep.ui.screens.files.components.Dialogs
import com.app.dockeep.ui.screens.files.components.FileListItem
import com.app.dockeep.ui.screens.files.components.FilesEmptyState
import com.app.dockeep.ui.screens.files.components.FilesFAB
import com.app.dockeep.ui.screens.files.components.FilesTopBar
import com.app.dockeep.ui.screens.files.components.SelectAllCheckbox
import com.app.dockeep.ui.screens.files.components.SelectionTopBar
import com.app.dockeep.utils.Constants.SETTINGS_ROUTE
import com.app.dockeep.utils.Helper.openDocument
import com.app.dockeep.utils.Helper.openDocumentIntent
import com.app.dockeep.utils.Helper.shareDocument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    path: String,
    uri: String,
    navController: NavController,
    onNavigate: (uri: String, path: String) -> Unit
) {
    val     mainVM:MainViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)

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
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showRenameFileDialog by remember { mutableStateOf(false) }
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showSortSheet by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var removeOnMove by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<DocumentItem?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val filesList by mainVM.files
    val dirList by mainVM.folders
    val loading by mainVM.loading
    var queryString by remember {
        mutableStateOf("")
    }
    var isInSelectionMode by remember {
        mutableStateOf(false)
    }
    val selectedItems = remember {
        mutableStateListOf<DocumentItem>()
    }

    val resetSelectionMode = {
        isInSelectionMode = false
        selectedItems.clear()
    }

    val effectiveItems = selectedItems.ifEmpty { selectedItem?.let { listOf(it) } ?: emptyList() }

    LaunchedEffect(key1 = Unit) {
        try {
            mainVM.loadFiles(uri)
            resetSelectionMode()
        } catch (_: Exception) {

        }
    }

    LaunchedEffect(
        key1 = isInSelectionMode,
        key2 = selectedItems.size,
    ) {
        if (isInSelectionMode && selectedItems.isEmpty()) {
            isInSelectionMode = false
        }
    }

    BackHandler(navController.previousBackStackEntry != null || isInSelectionMode) {
        if (isInSelectionMode) resetSelectionMode()
        else navController.popBackStack()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection),
        floatingActionButton = {
            FilesFAB {
                filePathLauncher.launch(openDocumentIntent)
            }
        },
        topBar = {
            if (isInSelectionMode) {
                SelectionTopBar(
                    selectedItems = selectedItems,
                    resetSelectionMode = resetSelectionMode,
                    openOptionsClick = {
                        showOptionsSheet = true
                        selectedItem = selectedItems[0]
                    },
                )
            } else {
                FilesTopBar(
                    title = path,
                    displayBackIcon = navController.previousBackStackEntry != null,
                    scrollBehaviour = scrollBehaviour,
                    onCreateFolder = {
                        showCreateFolderDialog = true
                    },
                    onNavigateSettings = {
                        navController.navigate(SETTINGS_ROUTE)
                    },
                    onGoBack = {
                        navController.popBackStack()
                    },
                    onSort = {
                        showSortSheet = true
                    }
                )
            }
        },

        ) { innerPadding ->


        LaunchedEffect(lifecycleState) {

            if (lifecycleState == Lifecycle.State.RESUMED) {
                if (mainVM.getContentPathUri().isNullOrBlank() || !mainVM.rootExists()) {
                    if (!mainVM.launched) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                        contentPathLauncher.launch(intent)
                    }
                } else if(queryString.isBlank()) {
                    mainVM.loadFiles(uri)
                    resetSelectionMode()
                }
            }
        }

        if (showSortSheet) {
            SortBottomSheet(
                initial = mainVM.getSortType(),
                onDismiss = {
                    showSortSheet = false
                },
                onSelect = {
                    mainVM.sortFiles(it)
                    showSortSheet = false
                }
            )
        }

        if (showOptionsSheet) {
            OptionsBottomSheet(
                items = effectiveItems,
                onDismiss = {
                    showOptionsSheet = false
                },
                onRename = {
                    showRenameFileDialog = true
                },
                onDelete = {
                    showConfirmDelete = true
                },
                onOpen = { item ->
                    context.openDocument(item.uri, item.mimeType)
                },
                onShare = { items ->
                    context.shareDocument(
                        uris = items.map { it.uri }.toList(),
                        mimeTypes = items.map { it.mimeType }
                    )
                },
                onMove = { files, delete ->
                    removeOnMove = delete
                    showMoveDialog = true
                },
            )
        }

        Dialogs(
            effectiveItems = effectiveItems,
            showConfirmDelete = showConfirmDelete,
            onDeleteConfirm = {
                mainVM.deleteFiles(uri, effectiveItems.map { it.uri })
                showConfirmDelete = false
                resetSelectionMode()
            },
            dismissDelete = { showConfirmDelete = false },
            showCreateFolder = showCreateFolderDialog,
            onCreateFolderConfirm = {
                mainVM.createFolder(
                    uri, it
                )
                showCreateFolderDialog = false
            },
            dismissCreateFolder = { showCreateFolderDialog = false },
            showRename = showRenameFileDialog,
            onConfirmRename = {
                selectedItem?.uri?.let { doc ->
                    mainVM.renameFile(
                        uri, doc, it
                    )
                }
                showRenameFileDialog = false
            },
            dismissRename = { showRenameFileDialog = false },
            showMove = showMoveDialog,
            onMoveConfirm = { folderUri ->
                mainVM.importFiles(
                    effectiveItems.map { it.uri }, folderUri, false, removeOnMove, uri
                )
                showMoveDialog = false
                resetSelectionMode()
            },
            dismissMove = { showMoveDialog = false },
            dirList = dirList,
            rootUri = uri,
            removeOnMove = removeOnMove
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppSearchBar(
                onSearch = {
                    queryString = it
                    mainVM.searchFiles(it, uri)
                },
                query = queryString
            )

            if (loading) {
                LoadingDialog()
            }
            if (filesList.isEmpty()) {
                FilesEmptyState()
            }

            Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isInSelectionMode) {
                        item {
                            SelectAllCheckbox { isChecked ->
                                if (isChecked) {
                                    selectedItems.removeAll(filesList)
                                    val itemsToAdd = filesList.filterNot { it in selectedItems }
                                    selectedItems.addAll(itemsToAdd)
                                } else selectedItems.removeAll(filesList)
                            }
                        }
                    }

                    item {
                        Spacer(
                            modifier = Modifier.height(10.dp)
                        )
                    }

                    items(filesList) { item ->
                        val isSelected = selectedItems.contains(item)
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
                            isSelected = isSelected,
                            selectionMode = isInSelectionMode,
                            addToSelected = {
                                selectedItems.add(item)
                            },
                            removeFromSelected = {
                                selectedItems.remove(item)
                            },
                            onLongClick = {
                                isInSelectionMode = true
                                selectedItems.add(item)
                            },
                        )
                    }
                }
            }
        }
    }
}