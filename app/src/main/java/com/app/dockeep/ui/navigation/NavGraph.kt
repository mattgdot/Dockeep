package com.app.dockeep.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.app.dockeep.ui.screens.files.FilesScreen
import com.app.dockeep.ui.screens.onboard.OnboardingScreen
import com.app.dockeep.ui.screens.settings.SettingsScreen
import com.app.dockeep.utils.Constants.FILES_DIR
import com.app.dockeep.utils.Constants.FILES_ROUTE
import com.app.dockeep.utils.Constants.GUIDE_ROUTE
import com.app.dockeep.utils.Constants.PATH_ARG
import com.app.dockeep.utils.Constants.SETTINGS_ROUTE
import com.app.dockeep.utils.Constants.URI_ARG


@Composable
fun NavGraph(navController: NavHostController, firstStart: Boolean) {
    val startRoute = if(firstStart) GUIDE_ROUTE else "${FILES_ROUTE}/${FILES_DIR}/"
    NavHost(navController = navController, startDestination = startRoute) {
        composable("$FILES_ROUTE/{${PATH_ARG}}/{${URI_ARG}}") { navBackStackEntry ->
            val path = navBackStackEntry.arguments?.getString(PATH_ARG).orEmpty()

            val uri = navBackStackEntry.arguments?.getString(URI_ARG).orEmpty()
                .replace('|', '%')
                .let(Uri::parse)

            FilesScreen(
                path = path, uri = uri.toString(),
                onNavigate = { uri, path ->
                    val encodedUri = Uri.encode(
                        uri
                            .replace('%', '|')
                    )
                    navController.navigate("$FILES_ROUTE/${path}/${encodedUri}")

                },
                navController = navController,
            )
        }

        composable(SETTINGS_ROUTE) {
            SettingsScreen(
                onGoBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(GUIDE_ROUTE) {
            OnboardingScreen(
                onDone = {
                    navController.navigate("${FILES_ROUTE}/${FILES_DIR}/") {
                        popUpTo(GUIDE_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}