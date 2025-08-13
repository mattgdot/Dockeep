package com.app.dockeep.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.app.dockeep.ui.screens.files.FilesScreen
import com.app.dockeep.utils.Constants.FILES_DIR
import com.app.dockeep.utils.Constants.FILES_ROUTE
import com.app.dockeep.utils.Constants.PATH_ARG
import com.app.dockeep.utils.Constants.URI_ARG


@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "${FILES_ROUTE}/${FILES_DIR}/") {
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
    }
}