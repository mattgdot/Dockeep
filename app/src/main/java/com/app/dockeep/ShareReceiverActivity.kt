package com.app.dockeep

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.dockeep.ui.Application
import com.app.dockeep.ui.MainViewModel
import com.app.dockeep.ui.components.LargeDropdownMenu
import com.app.dockeep.ui.theme.DockeepTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareReceiverActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DockeepTheme {
                val mainVM: MainViewModel = hiltViewModel()
                val dirs by mainVM.folders
                var selectedIndex by rememberSaveable { mutableIntStateOf(0) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    AlertDialog(
                        icon = {
                            Icon(Icons.Default.Add, contentDescription = null)
                        },
                        title = {
                            Text(text = "Import file(s)")
                        },
                        text = {

                            LargeDropdownMenu(
                                label = "Folder",
                                items = dirs,
                                selectedIndex = selectedIndex,
                                selectedItemToString = {
                                    it.first
                                },
                                onItemSelected = { index, item ->
                                    selectedIndex = index
                                    //mainViewModel.setCountryCode(index)
                                },
                                modifier = Modifier.padding(10.dp)
                            )
                        },
                        onDismissRequest = {
                            finish()
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    intent?.let { intent ->
                                        if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
                                            mainVM.importFiles(intent, dirs[selectedIndex].second.toString())
                                            finish()
                                        }
                                    }
                                },
                            ) {
                                Text("Done")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    finish()
                                }) {
                                Text("Cancel")
                            }
                        },
                    )
                }
            }
        }
    }
}