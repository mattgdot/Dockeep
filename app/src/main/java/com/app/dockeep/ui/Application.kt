package com.app.dockeep.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Application(mainVM: MainViewModel = hiltViewModel()) {
    val startForResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data?.data
                mainVM.setContentPathUri(intent.toString())
            }
        }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LaunchedEffect(key1 = Unit) {
                if (mainVM.getContentPathUri().isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    startForResult.launch(intent)
                }
            }

            Text("dockeep. WIP")
        }
    }
}