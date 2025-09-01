package com.app.dockeep.ui.screens.files

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.app.dockeep.ui.components.ConfirmActionDialog
import com.app.dockeep.ui.components.LoadingDialog
import com.app.dockeep.ui.components.OptionsBottomSheet
import com.app.dockeep.ui.components.SelectFolderDialog
import com.app.dockeep.ui.components.TextInputDialog
import com.app.dockeep.ui.screens.files.components.FileListItem
import com.app.dockeep.ui.screens.files.components.FilesEmptyState
import com.app.dockeep.ui.screens.files.components.FilesFAB
import com.app.dockeep.ui.screens.files.components.FilesTopBar
import com.app.dockeep.ui.screens.files.components.SelectionTopBar
import com.app.dockeep.utils.Constants.SETTINGS_ROUTE
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
    var showMoveDialog by rememberSaveable { mutableStateOf(false) }
    var removeOnMove by rememberSaveable { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<DocumentItem?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    val filesList by mainVM.files
    val loading by mainVM.loading

    var selectAllChecked by remember { mutableStateOf(false) }

    var isInSelectionMode by remember {
        mutableStateOf(false)
    }
    val selectedItems = remember {
        mutableStateListOf<DocumentItem>()
    }

    val resetSelectionMode = {
        isInSelectionMode = false
        selectedItems.clear()
        selectAllChecked = false
    }

    val selectOrDeselect = {
        if (selectAllChecked) {
            selectedItems.removeAll(filesList)
            val itemsToAdd = filesList.filterNot { it in selectedItems }
            selectedItems.addAll(itemsToAdd)
        } else selectedItems.removeAll(filesList)
    }

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
            selectAllChecked = false
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
            if (isInSelectionMode) {
                SelectionTopBar(
                    selectedItems = selectedItems,
                    resetSelectionMode = resetSelectionMode,
                    openOptionsClick = {
                        showOptionsSheet = true
                        selectedItem = selectedItems[0]
                    })
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
                    })
            }
        },

        ) { innerPadding ->

        BackHandler(navController.previousBackStackEntry != null || isInSelectionMode) {
            if (isInSelectionMode) resetSelectionMode()
            else navController.popBackStack()
        }

        LaunchedEffect(lifecycleState) {
            if (lifecycleState == Lifecycle.State.RESUMED) {
                if (mainVM.getContentPathUri().isNullOrBlank() || !mainVM.rootExists()) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    contentPathLauncher.launch(intent)
                } else {
                    mainVM.loadFiles(uri)
                    resetSelectionMode()
                }
            }
        }

        if (showConfirmDelete) {
            val items = selectedItems.ifEmpty { selectedItem?.let { listOf(it) } ?: listOf() }

            ConfirmActionDialog(
                title = "Delete ${items.size} item(s)?",
                message = "This action can not be undone.",
                onDismiss = { showConfirmDelete = false },
                onConfirm = {
                    mainVM.deleteFiles(uri, items.map { it.uri })
                    showConfirmDelete = false
                    resetSelectionMode()
                },
            )

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

        if (showMoveDialog) {
            val dirs by mainVM.folders
            val items = selectedItems.ifEmpty { selectedItem?.let { listOf(it) } ?: listOf() }
            var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
            val dirsFiltered = dirs.filterNot { fu ->
                fu.second.toString() == uri || items.any { it.uri == fu.second }
            }

            SelectFolderDialog(
                title = if (removeOnMove) "Move" else "Copy",
                folders = dirsFiltered,
                onFolderSelected = {
                    selectedIndex = it
                },
                selectedIndex = selectedIndex,
                onConfirm = {
                    val folderUri = dirsFiltered.getOrNull(selectedIndex)?.second?.toString() ?: ""

                    mainVM.importFiles(
                        items.map { it.uri }, folderUri, false, removeOnMove, uri
                    )
                    showMoveDialog = false
                    resetSelectionMode()

                },
                onDismiss = { showMoveDialog = false },
            )
        }

        if (showOptionsSheet) {
            OptionsBottomSheet(
                items = selectedItems.ifEmpty { selectedItem?.let { listOf(it) } ?: listOf() },
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
                onOpen = { item ->
                    context.openDocument(item.uri, item.mimeType)
                    showOptionsSheet = false
                },
                onShare = { items ->
                    context.shareDocument(items.map { it.uri }.toList(), items.map { it.mimeType })
                    showOptionsSheet = false
                },
                onMove = { files, delete ->
                    removeOnMove = delete
                    showMoveDialog = true
                    showOptionsSheet = false
                })
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (filesList.isEmpty()) {
                FilesEmptyState()
            } else {
                if (loading) {
                    LoadingDialog()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {

                    if (isInSelectionMode) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 15.dp)
                            ) {
                                Checkbox(
                                    checked = selectAllChecked,
                                    onCheckedChange = {
                                        selectAllChecked = !selectAllChecked
                                        selectOrDeselect()
                                    },
                                    colors = CheckboxDefaults.colors()
                                        .copy(uncheckedBorderColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    "Select all",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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