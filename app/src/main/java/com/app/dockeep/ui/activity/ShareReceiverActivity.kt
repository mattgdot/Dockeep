package com.app.dockeep.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.dockeep.ui.MainViewModel
import com.app.dockeep.ui.components.ShareImportDialog
import com.app.dockeep.ui.theme.DockeepTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shareIntent = intent

        setContent {
            DockeepTheme {
                val mainVM: MainViewModel = hiltViewModel()
                val dirs by mainVM.folders
                var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

                ShareImportDialog(
                    folders = dirs,
                    selectedIndex = selectedIndex,
                    onFolderSelected = { index -> selectedIndex = index },
                    onConfirm = {
                        shareIntent?.let { intent ->
                            if (intent.action == Intent.ACTION_SEND ||
                                intent.action == Intent.ACTION_SEND_MULTIPLE
                            ) {
                                val folderUri =
                                    dirs.getOrNull(selectedIndex)?.second?.toString() ?: return@let
                                mainVM.importFiles(intent, folderUri)
                                finish()
                            }
                        }
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }
}