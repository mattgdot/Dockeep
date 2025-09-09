package com.app.dockeep.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.app.dockeep.utils.ThemeMode

@Composable
fun ThemeSelectionDialog(
    onDismiss: () -> Unit,
    onSubmit: (ThemeMode) -> Unit,
    themeOptions: List<ThemeMode>,
    initialTheme: ThemeMode
) {
    var selectedTheme by remember {
        mutableStateOf(initialTheme)
    }

    AlertDialog(title = {
        Text(text = "Choose Theme")
    }, text = {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            themeOptions.forEach { theme ->
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTheme = theme }) {

                    RadioButton(selected = (theme == selectedTheme),
                        onClick = { selectedTheme = theme })
                    Text(
                        text = theme.text,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }, onDismissRequest = {
        onDismiss()
    }, dismissButton = {
        TextButton(onClick = {
            onDismiss()
        }) {
            Text("Cancel")
        }
    }, confirmButton = {
        TextButton(onClick = {
            onSubmit(selectedTheme)
            onDismiss()
        }) {
            Text("Done")
        }
    })
}