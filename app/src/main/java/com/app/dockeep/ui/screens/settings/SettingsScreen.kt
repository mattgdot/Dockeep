package com.app.dockeep.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.dockeep.ui.MainViewModel
import com.app.dockeep.ui.components.ThemeSelectionDialog
import com.app.dockeep.utils.Helper.getAppVersion
import com.app.dockeep.utils.ThemeMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onGoBack: () -> Unit,
) {
    val mainVM:MainViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
    val theme by mainVM.theme.collectAsState()

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
            var openThemeDialog by remember { mutableStateOf(false) }
            val themeOptions = listOf(ThemeMode.AUTO, ThemeMode.DARK, ThemeMode.LIGHT)
            val uriHandler = LocalUriHandler.current

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 15.dp)
                ) {
                    ListItem(headlineContent = {
                        Text(text = "App Theme")
                    }, leadingContent = {
                        Icon(Icons.Outlined.Settings, contentDescription = null)
                    }, modifier = Modifier.clickable {
                        openThemeDialog = true
                    })

                    ListItem(headlineContent = {
                        Text(text = "Share App")
                    }, leadingContent = {
                        Icon(Icons.Outlined.Share, contentDescription = null)
                    }, modifier = Modifier.clickable {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT, "https://github.com/mattgdot/Dockeep"
                            )
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        try {
                            context.startActivity(shareIntent)
                        } catch (_: Exception) {
                            Toast.makeText(
                                context, "Can't open link", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })


                    ListItem(headlineContent = {
                        Text(text = "Contact Developer")
                    }, leadingContent = {
                        Icon(Icons.Outlined.Email, contentDescription = null)
                    }, modifier = Modifier.clickable {
                        val emailIntent = Intent(
                            Intent.ACTION_SENDTO,
                            "mailto:decosoftapps@gmail.com".toUri()
                        )
                        try {
                            context.startActivity(emailIntent)
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context, "Can't open", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                    ListItem(headlineContent = {
                        Text(text = "Terms & Conditions")
                    }, leadingContent = {
                        Icon(Icons.Outlined.VerifiedUser, contentDescription = null)
                    }, modifier = Modifier.clickable {
                        try {
                            uriHandler.openUri("https://github.com/mattgdot/Dockeep/blob/main/terms_conditions.md")
                        } catch (_: Exception) {
                            Toast.makeText(
                                context, "Can't open link", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                    ListItem(headlineContent = {
                        Text(text = "Privacy Policy")
                    }, leadingContent = {
                        Icon(Icons.Outlined.Lock, contentDescription = null)
                    }, modifier = Modifier.clickable {
                        try {
                            uriHandler.openUri("https://github.com/mattgdot/Dockeep/blob/main/privacy_policy.md")
                        } catch (_: Exception) {
                            Toast.makeText(
                                context, "Can't open link", Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
                }
            }

            if (openThemeDialog) {
                ThemeSelectionDialog(
                    onDismiss = { openThemeDialog = false }, onSubmit = { theme ->
                        mainVM.setAppTheme(theme)
                    }, themeOptions = themeOptions, initialTheme = theme
                )
            }


            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
            ) {
                Text("Version: ${context.getAppVersion()?.versionName ?: ""}")
            }
        }
    }
}
