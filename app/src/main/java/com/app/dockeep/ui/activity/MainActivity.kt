package com.app.dockeep.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.app.dockeep.ui.MainViewModel
import com.app.dockeep.ui.navigation.NavGraph
import com.app.dockeep.ui.screens.lock.LockScreen
import com.app.dockeep.ui.theme.DockeepTheme
import com.app.dockeep.utils.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainVM:MainViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
            val theme by mainVM.theme.collectAsState()
            val lock by mainVM.lockChecked
            DockeepTheme(
                darkTheme = when (theme) {
                    ThemeMode.AUTO -> isSystemInDarkTheme()
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                }
            ) {
                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
                var showPrompt by remember { mutableStateOf(false) }

//                LaunchedEffect(lifecycleState) {
//                    if (lifecycleState == Lifecycle.State.RESUMED && lock) {
//                       // showPrompt = true
//                    }
//                }

                if(showPrompt) {
                    LockScreen(
                        onAuthSuccess = {
                            showPrompt = false
                        }
                    )
                } else {
                    val navController = rememberNavController()
                    NavGraph(navController)
                }
            }
        }
    }
}