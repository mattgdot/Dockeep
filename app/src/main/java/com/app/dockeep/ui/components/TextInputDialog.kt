package com.app.dockeep.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextInputDialog(
    initialText: String = "",
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length))) }
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    AlertDialog(
        title = {
            Text(text = title)
        },
        text = {
            OutlinedTextField(
                value = textFieldValue,
                label = { Text("Name") },
                onValueChange = { textFieldValue = it },
                textStyle = TextStyle(
                    fontSize = 18.sp
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .padding(5.dp)
                    .focusable(interactionSource = interactionSource)
                    .focusRequester(focusRequester)
                ,
            )
        },
        onDismissRequest = {
            onDismiss()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(textFieldValue.text)
                }) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }) {
                Text("Cancel")
            }
        },
    )
}