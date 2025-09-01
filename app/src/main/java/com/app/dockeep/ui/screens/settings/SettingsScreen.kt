package com.app.dockeep.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.dockeep.utils.Helper.getAppName
import com.app.dockeep.utils.Helper.getAppVersion


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onGoBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") }, navigationIcon = {
                IconButton(onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            })
        }) {
        BackHandler {
            onGoBack()
        }

        Box(
            modifier = Modifier.padding(it)
        ) {
            val context = LocalContext.current

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp)
                ) {
                    Text("${context.getAppName()} • Version: ${context.getAppVersion()?.versionNumber}")
                }
            }
        }
    }
}