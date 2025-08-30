package com.app.dockeep.ui.screens.files.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.app.dockeep.model.DocumentItem
import com.app.dockeep.utils.Helper.humanReadableSize

@Composable
fun FileListItem(
    item: DocumentItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                item.name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontWeight = if (item.isFolder) FontWeight.Bold else FontWeight.Normal
            )
        },
        supportingContent = {
            Text(if (item.isFolder) "Folder" else "${humanReadableSize(item.size!!)} • ${item.mimeType}", overflow = TextOverflow.Ellipsis, maxLines = 1)
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.isFolder) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = null
                )
            }
        },
        trailingContent = {
            IconButton(onClick = onMoreClick) { Icon(Icons.Default.MoreVert, null) }
        },
        modifier = Modifier
            .padding(vertical = 5.dp)
            .clickable { onClick() }
    )
}