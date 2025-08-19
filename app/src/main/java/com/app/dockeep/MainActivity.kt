package com.app.dockeep

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import com.app.dockeep.ui.Application
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.let { intent ->
            if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
                    val uriList = mutableListOf<Uri>()
                    var isRoot = false

                    for (i in 0..((intent.clipData?.itemCount?.minus(1)) ?: 0)) {
                        intent.clipData?.getItemAt(i)?.uri?.let { uri -> uriList.add(uri) }
                        intent.data?.let { uri -> uriList.add(uri) }
                    }

                    println(uriList)
            }
        }

        setContent {
            Application()
        }
    }
}