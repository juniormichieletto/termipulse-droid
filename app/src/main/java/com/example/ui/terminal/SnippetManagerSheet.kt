package com.example.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SavedSnippet
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalPrimary
import com.example.ui.theme.TerminalSecondary
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalTextPrimary
import com.example.ui.theme.TerminalTextSecondary

@Composable
fun SnippetManagerSheet(
    snippets: List<SavedSnippet>,
    onExecuteSnippet: (SavedSnippet) -> Unit,
    onSaveSnippet: (SavedSnippet) -> Unit,
    onDeleteSnippet: (SavedSnippet) -> Unit,
    onDismiss: () -> Unit
) {
    var isAddingNew by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var description by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = TerminalSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TerminalBorder, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAddingNew) "New Quick Snippet" else "Bash Command Snippets",
                        color = TerminalTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    if (!isAddingNew) {
                        IconButton(
                            onClick = {
                                title = ""
                                command = ""
                                category = "Custom"
                                description = ""
                                isAddingNew = true
                            },
                            modifier = Modifier.testTag("add_snippet_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Snippet",
                                tint = TerminalPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isAddingNew) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title (e.g. Restart Nginx)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerminalPrimary,
                                unfocusedBorderColor = TerminalBorder
                            )
                        )

                        OutlinedTextField(
                            value = command,
                            onValueChange = { command = it },
                            label = { Text("Bash Command (e.g. systemctl restart nginx)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerminalPrimary,
                                unfocusedBorderColor = TerminalBorder
                            )
                        )

                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerminalPrimary,
                                unfocusedBorderColor = TerminalBorder
                            )
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description / Notes") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TerminalPrimary,
                                unfocusedBorderColor = TerminalBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(onClick = { isAddingNew = false }) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (title.isNotBlank() && command.isNotBlank()) {
                                        onSaveSnippet(
                                            SavedSnippet(
                                                title = title,
                                                command = command,
                                                category = category.ifBlank { "General" },
                                                description = description
                                            )
                                        )
                                        isAddingNew = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalPrimary)
                            ) {
                                Text("Save Snippet", color = TerminalSurface)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .height(320.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(snippets) { snippet ->
                            SnippetListItem(
                                snippet = snippet,
                                onRun = {
                                    onExecuteSnippet(snippet)
                                    onDismiss()
                                },
                                onDelete = { onDeleteSnippet(snippet) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = onDismiss) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SnippetListItem(
    snippet: SavedSnippet,
    onRun: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurfaceVariant, RoundedCornerShape(10.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(TerminalSecondary.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, TerminalSecondary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = TerminalSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = snippet.title,
                    color = TerminalTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "[${snippet.category}]",
                    color = TerminalPrimary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = snippet.command,
                color = TerminalSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }

        IconButton(
            onClick = onRun,
            modifier = Modifier
                .size(32.dp)
                .background(TerminalPrimary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Run Command",
                tint = TerminalSurface,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Snippet",
                tint = TerminalError,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
