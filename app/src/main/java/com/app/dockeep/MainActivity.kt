package com.app.dockeep

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.app.dockeep.ui.Application
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        when {
            intent?.action == Intent.ACTION_SEND -> {

            }

            intent?.action == Intent.ACTION_SEND_MULTIPLE -> {

            }

            else -> {

            }
        }

        setContent {
            Application()
        }
    }
}