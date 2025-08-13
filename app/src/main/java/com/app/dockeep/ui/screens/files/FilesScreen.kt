package com.app.dockeep.ui.screens.files

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FilesFAB {
                filePathLauncher.launch(mainVM.openFileIntent)
            }
        },
        topBar = {
            FilesTopBar(
                title = path, displayBackIcon = navController.previousBackStackEntry != null
            ) {
                navController.popBackStack()
            }
        },

        ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val filesList by mainVM.files

            LaunchedEffect(key1 = Unit) {
                if (mainVM.getContentPathUri().isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    contentPathLauncher.launch(intent)
                } else {
                    mainVM.loadFiles(uri)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filesList) { item ->
                    ListItem(
                        headlineContent = { Text(item.name) },
                        supportingContent = { Text(if (item.isFolder) "Folder" else item.mimeType) },
                        modifier = Modifier.clickable {
                            if (item.isFolder) {
                                onNavigate(item.uri.toString(), item.name)
                            }
                        },
                    )
                }
            }
        }
    }
}
