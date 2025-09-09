package com.app.dockeep.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSearchBar(
    onSearch: (query: String) -> Unit,
    query: String
) {

    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val focusManager = LocalFocusManager.current

    if(query.isBlank()) {
        focusManager.clearFocus()
    }

    SearchBar(
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier
                    .focusable(interactionSource = interactionSource)
                    .focusRequester(focusRequester),
                query = query,
                onQueryChange = { newQueryString ->
                    onSearch(newQueryString)
                },
                onSearch = {

                },
                expanded = false,
                onExpandedChange = {  },
                placeholder = {
                    Text(text = "Search")
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = {
                            onSearch("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
            )
        },
        expanded = false,
        onExpandedChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(bottom = 15.dp)
        ,
        content = {
        },
    )

}