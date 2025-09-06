package com.app.dockeep.ui.screens.files.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectAllCheckbox(
    toggleSelectAll: (Boolean) -> Unit
) {
    var selectAllChecked by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 15.dp)
    ) {
        Checkbox(
            checked = selectAllChecked,
            onCheckedChange = {
                selectAllChecked = !selectAllChecked
                toggleSelectAll(selectAllChecked)
            },
            colors = CheckboxDefaults.colors()
                .copy(uncheckedBorderColor = MaterialTheme.colorScheme.primary)
        )
        Text(
            "Select all",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}