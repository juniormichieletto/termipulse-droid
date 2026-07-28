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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SshProfile
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalError
import com.example.ui.theme.TerminalPrimary
import com.example.ui.theme.TerminalSecondary
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalTextPrimary
import com.example.ui.theme.TerminalTextSecondary

@Composable
fun ProfileManagerDialog(
    profiles: List<SshProfile>,
    profileToEdit: SshProfile?,
    onConnect: (SshProfile) -> Unit,
    onSaveProfile: (SshProfile) -> Unit,
    onDeleteProfile: (SshProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var isEditingMode by remember { mutableStateOf(profileToEdit != null) }

    var name by remember { mutableStateOf(profileToEdit?.name ?: "") }
    var host by remember { mutableStateOf(profileToEdit?.host ?: "") }
    var portText by remember { mutableStateOf((profileToEdit?.port ?: 22).toString()) }
    var username by remember { mutableStateOf(profileToEdit?.username ?: "") }
    var authType by remember { mutableStateOf(profileToEdit?.authType ?: "PASSWORD") }
    var passwordOrKey by remember { mutableStateOf(profileToEdit?.passwordOrKey ?: "") }
    var badgeColorHex by remember { mutableStateOf(profileToEdit?.badgeColorHex ?: "#00E676") }
    var isSandbox by remember { mutableStateOf(profileToEdit?.isSandbox ?: false) }

    val colorOptions = listOf("#00E676", "#00E5FF", "#FFC107", "#E040FB", "#FF5252", "#3D5AFE")

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
                        text = if (isEditingMode) "SSH Host Profile" else "Saved Remote Hosts",
                        color = TerminalTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    if (!isEditingMode) {
                        IconButton(
                            onClick = {
                                name = "New Remote Host"
                                host = ""
                                portText = "22"
                                username = "ubuntu"
                                passwordOrKey = ""
                                isSandbox = false
                                isEditingMode = true
                            },
                            modifier = Modifier.testTag("add_new_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Profile",
                                tint = TerminalPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isEditingMode) {
                    // Profile Edit Form
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Profile Label (e.g. Production Web)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TerminalPrimary,
                                    unfocusedBorderColor = TerminalBorder
                                )
                            )
                        }

                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = host,
                                    onValueChange = { host = it },
                                    label = { Text("Host IP / Domain") },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.7f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalPrimary,
                                        unfocusedBorderColor = TerminalBorder
                                    )
                                )

                                OutlinedTextField(
                                    value = portText,
                                    onValueChange = { portText = it },
                                    label = { Text("Port") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.3f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = TerminalPrimary,
                                        unfocusedBorderColor = TerminalBorder
                                    )
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TerminalPrimary,
                                    unfocusedBorderColor = TerminalBorder
                                )
                            )
                        }

                        item {
                            Text("Authentication Type", color = TerminalTextSecondary, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = authType == "PASSWORD",
                                    onClick = { authType = "PASSWORD" },
                                    colors = RadioButtonDefaults.colors(selectedColor = TerminalPrimary)
                                )
                                Text("Password", color = TerminalTextPrimary, fontSize = 13.sp)

                                Spacer(modifier = Modifier.width(16.dp))

                                RadioButton(
                                    selected = authType == "KEY",
                                    onClick = { authType = "KEY" },
                                    colors = RadioButtonDefaults.colors(selectedColor = TerminalPrimary)
                                )
                                Text("Private Key", color = TerminalTextPrimary, fontSize = 13.sp)
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = passwordOrKey,
                                onValueChange = { passwordOrKey = it },
                                label = { Text(if (authType == "PASSWORD") "Password" else "Private Key (PEM/OpenSSH)") },
                                singleLine = authType == "PASSWORD",
                                visualTransformation = if (authType == "PASSWORD") PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TerminalPrimary,
                                    unfocusedBorderColor = TerminalBorder
                                )
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Local Terminal Sandbox", color = TerminalTextPrimary, fontSize = 13.sp)
                                    Text("Simulates terminal offline without network", color = TerminalTextSecondary, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = isSandbox,
                                    onCheckedChange = { isSandbox = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = TerminalPrimary)
                                )
                            }
                        }

                        item {
                            Text("Badge Color", color = TerminalTextSecondary, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                colorOptions.forEach { hex ->
                                    val c = Color(android.graphics.Color.parseColor(hex))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(c, CircleShape)
                                            .border(
                                                width = if (badgeColorHex == hex) 2.dp else 0.dp,
                                                color = Color.White,
                                                shape = CircleShape
                                            )
                                            .clickable { badgeColorHex = hex }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { isEditingMode = false }
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val newProfile = SshProfile(
                                    id = profileToEdit?.id ?: 0L,
                                    name = name.ifBlank { "Remote Host" },
                                    host = host.ifBlank { "127.0.0.1" },
                                    port = portText.toIntOrNull() ?: 22,
                                    username = username.ifBlank { "ubuntu" },
                                    authType = authType,
                                    passwordOrKey = passwordOrKey,
                                    badgeColorHex = badgeColorHex,
                                    isSandbox = isSandbox
                                )
                                onSaveProfile(newProfile)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TerminalPrimary)
                        ) {
                            Text("Save Profile", color = TerminalSurface)
                        }
                    }
                } else {
                    // Profile List View
                    LazyColumn(
                        modifier = Modifier
                            .height(320.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profiles) { profile ->
                            ProfileListItem(
                                profile = profile,
                                onConnect = {
                                    onConnect(profile)
                                    onDismiss()
                                },
                                onEdit = {
                                    name = profile.name
                                    host = profile.host
                                    portText = profile.port.toString()
                                    username = profile.username
                                    authType = profile.authType
                                    passwordOrKey = profile.passwordOrKey
                                    badgeColorHex = profile.badgeColorHex
                                    isSandbox = profile.isSandbox
                                    isEditingMode = true
                                },
                                onDelete = { onDeleteProfile(profile) }
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
private fun ProfileListItem(
    profile: SshProfile,
    onConnect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val badgeColor = try {
        Color(android.graphics.Color.parseColor(profile.badgeColorHex))
    } catch (_: Exception) {
        TerminalPrimary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TerminalSurfaceVariant, RoundedCornerShape(10.dp))
            .border(1.dp, TerminalBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onConnect)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(badgeColor.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (profile.isSandbox) Icons.Default.Terminal else Icons.Default.Computer,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                color = TerminalTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = if (profile.isSandbox) "Local Sandbox Shell" else "${profile.username}@${profile.host}:${profile.port}",
                color = TerminalTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile",
                tint = TerminalSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Profile",
                tint = TerminalError,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
