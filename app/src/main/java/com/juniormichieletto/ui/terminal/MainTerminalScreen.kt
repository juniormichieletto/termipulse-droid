package com.juniormichieletto.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juniormichieletto.ui.theme.TerminalBackground
import com.juniormichieletto.ui.theme.TerminalPrimary
import com.juniormichieletto.ui.theme.TerminalSecondary
import com.juniormichieletto.ui.theme.TerminalSurface
import com.juniormichieletto.ui.theme.TerminalSurfaceVariant
import com.juniormichieletto.ui.theme.TerminalTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTerminalScreen(
    viewModel: TerminalViewModel
) {
    val tabs by viewModel.tabs.collectAsStateWithLifecycle()
    val activeTabIndex by viewModel.activeTabIndex.collectAsStateWithLifecycle()
    val isCtrlActive by viewModel.isCtrlActive.collectAsStateWithLifecycle()
    val isAltActive by viewModel.isAltActive.collectAsStateWithLifecycle()
    val fontSizeSp by viewModel.fontSizeSp.collectAsStateWithLifecycle()
    val commandInputText by viewModel.commandInputText.collectAsStateWithLifecycle()

    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()
    val savedSnippets by viewModel.savedSnippets.collectAsStateWithLifecycle()

    val showProfileDialog by viewModel.showProfileDialog.collectAsStateWithLifecycle()
    val showSnippetDialog by viewModel.showSnippetDialog.collectAsStateWithLifecycle()
    val selectedProfileForEdit by viewModel.selectedProfileForEdit.collectAsStateWithLifecycle()

    val activeTab = viewModel.getActiveTab()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(TerminalPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = TerminalBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "TermiPulse",
                                color = TerminalTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = activeTab?.hostLabel ?: "SSH Client",
                                color = TerminalSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openSnippetDialog() },
                        modifier = Modifier.testTag("top_snippets_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Saved Snippets",
                            tint = TerminalSecondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.openProfileDialog() },
                        modifier = Modifier.testTag("top_hosts_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Saved Hosts Profiles",
                            tint = TerminalPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TerminalSurface)
            )
        },
        containerColor = TerminalBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Multi-Tab Navigation Header
            TerminalTabBar(
                tabs = tabs,
                activeTabIndex = activeTabIndex,
                onTabSelected = { viewModel.selectTab(it) },
                onTabClosed = { viewModel.closeTab(it) },
                onAddTabClick = { viewModel.openProfileDialog() }
            )

            // Terminal Output Screen Canvas
            if (activeTab != null) {
                TerminalCanvasView(
                    activeTab = activeTab,
                    fontSizeSp = fontSizeSp,
                    onIncreaseFont = { viewModel.increaseFontSize() },
                    onDecreaseFont = { viewModel.decreaseFontSize() },
                    onClearTerminal = { viewModel.clearActiveTerminal() },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(TerminalBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No open terminal tabs.\nTap + to connect to a host or open a local sandbox.",
                        color = TerminalSurfaceVariant,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Mobile Control Key Bar (Ctrl, Alt, Arrows, Special Keys)
            ControlKeyBar(
                isCtrlActive = isCtrlActive,
                isAltActive = isAltActive,
                onToggleCtrl = { viewModel.toggleCtrl() },
                onToggleAlt = { viewModel.toggleAlt() },
                onKeyClick = { key ->
                    if (isCtrlActive) {
                        viewModel.sendCtrlKey(key)
                    } else {
                        viewModel.sendSpecialKey(key)
                    }
                }
            )

            // Command Input Field Bar
            CommandInputBar(
                inputText = commandInputText,
                onInputChanged = { viewModel.onCommandInputChanged(it) },
                onSend = { viewModel.sendCurrentCommand() },
                onOpenSnippetsClick = { viewModel.openSnippetDialog() },
                onHistoryUp = { viewModel.sendSpecialKey("▲") },
                onHistoryDown = { viewModel.sendSpecialKey("▼") }
            )
        }
    }

    // Saved Profiles Manager Dialog
    if (showProfileDialog) {
        ProfileManagerDialog(
            profiles = savedProfiles,
            profileToEdit = selectedProfileForEdit,
            onConnect = { profile -> viewModel.openNewTabFromProfile(profile) },
            onSaveProfile = { profile -> viewModel.saveProfile(profile) },
            onDeleteProfile = { profile -> viewModel.deleteProfile(profile) },
            onDismiss = { viewModel.closeProfileDialog() }
        )
    }

    // Quick Snippets Manager Dialog
    if (showSnippetDialog) {
        SnippetManagerSheet(
            snippets = savedSnippets,
            onExecuteSnippet = { snippet -> viewModel.executeSnippet(snippet) },
            onSaveSnippet = { snippet -> viewModel.saveSnippet(snippet) },
            onDeleteSnippet = { snippet -> viewModel.deleteSnippet(snippet) },
            onDismiss = { viewModel.closeSnippetDialog() }
        )
    }
}
