package com.app.dockeep.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.dockeep.ui.MainViewModel
import com.app.dockeep.ui.components.SelectFolderDialog
import com.app.dockeep.ui.theme.DockeepTheme
import com.app.dockeep.utils.Helper.extractUris
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
                SelectFolderDialog(
                    folders = dirs,
                    onConfirm = { folderUri ->
                        shareIntent?.let { intent ->
                            if (intent.action == Intent.ACTION_SEND ||
                                intent.action == Intent.ACTION_SEND_MULTIPLE
                            ) {
                                mainVM.importFiles(intent.extractUris(), folderUri)
                                finish()
                            }
                        }
                    },
                    onDismiss = { finish() },
                    title = "Import"
                )
            }
        }
    }
}