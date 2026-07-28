package com.juniormichieletto.ui.terminal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juniormichieletto.data.AppDatabase
import com.juniormichieletto.data.SavedSnippet
import com.juniormichieletto.data.SshProfile
import com.juniormichieletto.data.TerminalRepository
import com.juniormichieletto.service.TerminalSessionService
import com.juniormichieletto.terminal.SessionEvent
import com.juniormichieletto.terminal.SshSessionHandler
import com.juniormichieletto.terminal.TerminalLine
import com.juniormichieletto.terminal.TerminalTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TerminalRepository
    val savedProfiles: StateFlow<List<SshProfile>>
    val savedSnippets: StateFlow<List<SavedSnippet>>

    private val sessionHandlers = mutableMapOf<String, SshSessionHandler>()

    private val _tabs = MutableStateFlow<List<TerminalTab>>(emptyList())
    val tabs: StateFlow<List<TerminalTab>> = _tabs.asStateFlow()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex.asStateFlow()

    private val _isCtrlActive = MutableStateFlow(false)
    val isCtrlActive: StateFlow<Boolean> = _isCtrlActive.asStateFlow()

    private val _isAltActive = MutableStateFlow(false)
    val isAltActive: StateFlow<Boolean> = _isAltActive.asStateFlow()

    private val _fontSizeSp = MutableStateFlow(14)
    val fontSizeSp: StateFlow<Int> = _fontSizeSp.asStateFlow()

    private val _commandInputText = MutableStateFlow("")
    val commandInputText: StateFlow<String> = _commandInputText.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    private val _showSnippetDialog = MutableStateFlow(false)
    val showSnippetDialog: StateFlow<Boolean> = _showSnippetDialog.asStateFlow()

    private val _selectedProfileForEdit = MutableStateFlow<SshProfile?>(null)
    val selectedProfileForEdit: StateFlow<SshProfile?> = _selectedProfileForEdit.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TerminalRepository(database.sshProfileDao(), database.savedSnippetDao())

        savedProfiles = repository.allProfiles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedSnippets = repository.allSnippets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Initial default tab
        openNewSandboxTab()
    }

    fun openNewTabFromProfile(profile: SshProfile) {
        val newTab = TerminalTab(
            id = UUID.randomUUID().toString(),
            profileId = profile.id,
            title = profile.name,
            hostLabel = "${profile.username}@${profile.host}",
            username = profile.username,
            host = profile.host,
            port = profile.port,
            badgeColorHex = profile.badgeColorHex,
            isSandbox = profile.isSandbox,
            currentDirectory = profile.defaultDirectory
        )

        val currentList = _tabs.value.toMutableList()
        currentList.add(newTab)
        _tabs.value = currentList
        _activeTabIndex.value = currentList.size - 1

        val handler = SshSessionHandler(newTab.id, profile, viewModelScope)
        sessionHandlers[newTab.id] = handler

        observeSessionEvents(newTab.id, handler)
        handler.connect()

        updateForegroundService()
    }

    fun openNewSandboxTab() {
        val defaultProfile = SshProfile(
            name = "Local Terminal Sandbox",
            host = "127.0.0.1",
            port = 22,
            username = "termipulse",
            authType = "PASSWORD",
            badgeColorHex = "#00E676",
            isSandbox = true
        )
        openNewTabFromProfile(defaultProfile)
    }

    private fun observeSessionEvents(tabId: String, handler: SshSessionHandler) {
        viewModelScope.launch {
            handler.events.collect { event ->
                when (event) {
                    is SessionEvent.OutputReceived -> {
                        appendOutputToTab(tabId, event.text, event.isInput)
                    }
                    is SessionEvent.StatusChanged -> {
                        updateTabStatus(tabId, event.isConnected, event.message, event.isLongJobRunning)
                    }
                    is SessionEvent.DirectoryChanged -> {
                        updateTabDirectory(tabId, event.newPath)
                    }
                    is SessionEvent.ErrorOccurred -> {
                        appendOutputToTab(tabId, "\u001B[31m[ERROR]\u001B[0m ${event.error}", false)
                    }
                }
            }
        }
    }

    private fun appendOutputToTab(tabId: String, text: String, isInput: Boolean) {
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == tabId) {
                    val newLine = TerminalLine(rawText = text, isInput = isInput)
                    tab.copy(lines = tab.lines + newLine)
                } else tab
            }
        }
    }

    private fun updateTabStatus(tabId: String, isConnected: Boolean, message: String, isLongJobRunning: Boolean) {
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == tabId) {
                    tab.copy(
                        isConnected = isConnected,
                        statusMessage = message,
                        isLongJobRunning = isLongJobRunning
                    )
                } else tab
            }
        }
        updateForegroundService()
    }

    private fun updateTabDirectory(tabId: String, newPath: String) {
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == tabId) tab.copy(currentDirectory = newPath) else tab
            }
        }
    }

    fun selectTab(index: Int) {
        if (index in 0 until _tabs.value.size) {
            _activeTabIndex.value = index
        }
    }

    fun closeTab(index: Int) {
        val currentList = _tabs.value.toMutableList()
        if (index in 0 until currentList.size) {
            val closingTab = currentList[index]
            sessionHandlers[closingTab.id]?.disconnect()
            sessionHandlers.remove(closingTab.id)

            currentList.removeAt(index)
            _tabs.value = currentList

            if (currentList.isEmpty()) {
                openNewSandboxTab()
            } else {
                val newActiveIndex = (index - 1).coerceAtLeast(0).coerceAtMost(currentList.size - 1)
                _activeTabIndex.value = newActiveIndex
            }
        }
        updateForegroundService()
    }

    fun onCommandInputChanged(text: String) {
        _commandInputText.value = text
    }

    fun sendCurrentCommand() {
        val cmd = _commandInputText.value
        if (cmd.isNotBlank()) {
            val activeTab = getActiveTab() ?: return
            sessionHandlers[activeTab.id]?.sendCommand(cmd)

            // Add to command history
            _tabs.update { currentTabs ->
                currentTabs.map { tab ->
                    if (tab.id == activeTab.id) {
                        tab.copy(commandHistory = tab.commandHistory + cmd)
                    } else tab
                }
            }

            _commandInputText.value = ""
        }
    }

    fun executeSnippet(snippet: SavedSnippet) {
        val activeTab = getActiveTab() ?: return
        sessionHandlers[activeTab.id]?.sendCommand(snippet.command)
    }

    fun toggleCtrl() {
        _isCtrlActive.value = !_isCtrlActive.value
    }

    fun toggleAlt() {
        _isAltActive.value = !_isAltActive.value
    }

    fun sendCtrlKey(char: String) {
        val activeTab = getActiveTab() ?: return
        if (char.equals("c", ignoreCase = true)) {
            sessionHandlers[activeTab.id]?.sendCtrlC()
        } else {
            sessionHandlers[activeTab.id]?.sendCommand("CTRL+$char")
        }
        _isCtrlActive.value = false
    }

    fun sendSpecialKey(keySymbol: String) {
        val activeTab = getActiveTab() ?: return
        val handler = sessionHandlers[activeTab.id] ?: return

        when (keySymbol) {
            "Ctrl+C" -> handler.sendCtrlC()
            "Ctrl+L" -> handler.sendCommand("clear")
            "Ctrl+Z" -> handler.sendCommand("CTRL+Z")
            "Ctrl+D" -> handler.sendCommand("exit")
            "Tab" -> handler.sendCommand("  ")
            "Esc" -> handler.sendCommand("ESC")
            "▲" -> historyNavigate(-1)
            "▼" -> historyNavigate(1)
            else -> {
                _commandInputText.value += keySymbol
            }
        }
    }

    private fun historyNavigate(direction: Int) {
        val activeTab = getActiveTab() ?: return
        val history = activeTab.commandHistory
        if (history.isNotEmpty()) {
            val currentCmd = _commandInputText.value
            val index = history.indexOf(currentCmd)
            val newIndex = if (index == -1) {
                if (direction < 0) history.size - 1 else 0
            } else {
                (index + direction).coerceIn(0, history.size - 1)
            }
            _commandInputText.value = history[newIndex]
        }
    }

    fun increaseFontSize() {
        if (_fontSizeSp.value < 26) _fontSizeSp.value += 2
    }

    fun decreaseFontSize() {
        if (_fontSizeSp.value > 10) _fontSizeSp.value -= 2
    }

    fun clearActiveTerminal() {
        val activeTab = getActiveTab() ?: return
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == activeTab.id) tab.copy(lines = emptyList()) else tab
            }
        }
    }

    fun getActiveTab(): TerminalTab? {
        val index = _activeTabIndex.value
        val currentTabs = _tabs.value
        return if (index in currentTabs.indices) currentTabs[index] else null
    }

    fun openProfileDialog(profileToEdit: SshProfile? = null) {
        _selectedProfileForEdit.value = profileToEdit
        _showProfileDialog.value = true
    }

    fun closeProfileDialog() {
        _showProfileDialog.value = false
        _selectedProfileForEdit.value = null
    }

    fun saveProfile(profile: SshProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            closeProfileDialog()
        }
    }

    fun deleteProfile(profile: SshProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
            closeProfileDialog()
        }
    }

    fun openSnippetDialog() {
        _showSnippetDialog.value = true
    }

    fun closeSnippetDialog() {
        _showSnippetDialog.value = false
    }

    fun saveSnippet(snippet: SavedSnippet) {
        viewModelScope.launch {
            repository.saveSnippet(snippet)
            closeSnippetDialog()
        }
    }

    fun deleteSnippet(snippet: SavedSnippet) {
        viewModelScope.launch {
            repository.deleteSnippet(snippet)
        }
    }

    private fun updateForegroundService() {
        val activeCount = _tabs.value.count { it.isConnected }
        val hasLongJob = _tabs.value.any { it.isLongJobRunning }

        if (activeCount > 0) {
            TerminalSessionService.startService(getApplication(), activeCount, hasLongJob)
        } else {
            TerminalSessionService.stopService(getApplication())
        }
    }
}
