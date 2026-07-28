package com.juniormichieletto.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juniormichieletto.ui.theme.TerminalBorder
import com.juniormichieletto.ui.theme.TerminalPrimary
import com.juniormichieletto.ui.theme.TerminalSurface
import com.juniormichieletto.ui.theme.TerminalSurfaceVariant
import com.juniormichieletto.ui.theme.TerminalTextPrimary
import com.juniormichieletto.ui.theme.TerminalTextSecondary

@Composable
fun CommandInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onOpenSnippetsClick: () -> Unit,
    onHistoryUp: () -> Unit,
    onHistoryDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .border(width = 1.dp, color = TerminalBorder)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Snippets tray button
        IconButton(
            onClick = onOpenSnippetsClick,
            modifier = Modifier
                .size(38.dp)
                .background(TerminalSurfaceVariant, RoundedCornerShape(8.dp))
                .testTag("snippets_drawer_button")
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = "Quick Snippets",
                tint = TerminalPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // History Up/Down quick buttons
        IconButton(
            onClick = onHistoryUp,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous Command",
                tint = TerminalTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onHistoryDown,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Next Command",
                tint = TerminalTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Input Field
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChanged,
            placeholder = {
                Text(
                    text = "Type bash command...",
                    color = TerminalTextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = TerminalTextPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TerminalPrimary,
                unfocusedBorderColor = TerminalBorder,
                focusedContainerColor = TerminalSurfaceVariant,
                unfocusedContainerColor = TerminalSurfaceVariant
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .weight(1f)
                .testTag("command_input_field")
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Send Button
        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(38.dp)
                .background(TerminalPrimary, CircleShape)
                .testTag("send_command_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Execute Command",
                tint = TerminalSurface,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
