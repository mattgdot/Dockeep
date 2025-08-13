package com.app.dockeep.ui.screens.files

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable

@Composable
fun FilesFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
    ) {
        Icon(Icons.Default.Add, null)
    }
}