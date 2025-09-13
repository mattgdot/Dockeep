package com.app.dockeep.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
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
            val mainVM: MainViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
            val theme by mainVM.theme.collectAsState()
            val firstStart by mainVM.firstStart
            DockeepTheme(
                darkTheme = when (theme) {
                    ThemeMode.AUTO -> isSystemInDarkTheme()
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                }
            ) {

                val navController = rememberNavController()
                NavGraph(navController, firstStart)

            }
        }
    }
}