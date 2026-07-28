package com.juniormichieletto

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.juniormichieletto.data.SavedSnippet
import com.juniormichieletto.data.SshProfile
import com.juniormichieletto.ui.terminal.TerminalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TerminalViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: TerminalViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = TerminalViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateHasSandboxTab() {
        assertEquals(1, viewModel.tabs.value.size)
        assertEquals(0, viewModel.activeTabIndex.value)

        val activeTab = viewModel.getActiveTab()
        assertNotNull(activeTab)
        assertTrue(activeTab!!.isSandbox)
        assertEquals("termipulse", activeTab.username)
    }

    @Test
    fun testAddNewTabFromProfile() {
        val profile = SshProfile(
            id = 10,
            name = "My Remote Server",
            host = "1.2.3.4",
            port = 2222,
            username = "root",
            authType = "PASSWORD",
            passwordOrKey = "pass",
            badgeColorHex = "#448AFF",
            isSandbox = false
        )

        viewModel.openNewTabFromProfile(profile)

        assertEquals(2, viewModel.tabs.value.size)
        assertEquals(1, viewModel.activeTabIndex.value)

        val activeTab = viewModel.getActiveTab()
        assertNotNull(activeTab)
        assertEquals("1.2.3.4", activeTab!!.host)
        assertEquals(2222, activeTab.port)
        assertEquals("root", activeTab.username)
        assertFalse(activeTab.isSandbox)
    }

    @Test
    fun testSelectTab() {
        viewModel.openNewSandboxTab()
        assertEquals(2, viewModel.tabs.value.size)
        assertEquals(1, viewModel.activeTabIndex.value)

        viewModel.selectTab(0)
        assertEquals(0, viewModel.activeTabIndex.value)

        // Invalid index should do nothing
        viewModel.selectTab(99)
        assertEquals(0, viewModel.activeTabIndex.value)
    }

    @Test
    fun testCloseTab() {
        viewModel.openNewSandboxTab()
        assertEquals(2, viewModel.tabs.value.size)

        viewModel.closeTab(1)
        assertEquals(1, viewModel.tabs.value.size)
        assertEquals(0, viewModel.activeTabIndex.value)

        // Closing the last tab auto-opens a new sandbox tab
        viewModel.closeTab(0)
        assertEquals(1, viewModel.tabs.value.size)
        assertEquals(0, viewModel.activeTabIndex.value)
    }

    @Test
    fun testCommandInputAndSend() {
        viewModel.onCommandInputChanged("echo Hello World")
        assertEquals("echo Hello World", viewModel.commandInputText.value)

        viewModel.sendCurrentCommand()
        assertEquals("", viewModel.commandInputText.value)

        val activeTab = viewModel.getActiveTab()
        assertNotNull(activeTab)
        assertTrue(activeTab!!.commandHistory.contains("echo Hello World"))
    }

    @Test
    fun testExecuteSnippet() {
        val snippet = SavedSnippet(id = 1, title = "Check Docker", command = "docker ps", category = "DEVOPS")
        viewModel.executeSnippet(snippet)

        val activeTab = viewModel.getActiveTab()
        assertNotNull(activeTab)
    }

    @Test
    fun testSpecialKeysAndHistoryNavigation() {
        viewModel.toggleCtrl()
        assertTrue(viewModel.isCtrlActive.value)
        viewModel.toggleCtrl()
        assertFalse(viewModel.isCtrlActive.value)

        viewModel.toggleAlt()
        assertTrue(viewModel.isAltActive.value)

        viewModel.sendCtrlKey("c")
        assertFalse(viewModel.isCtrlActive.value)

        viewModel.sendSpecialKey("Ctrl+C")
        viewModel.sendSpecialKey("Ctrl+L")
        viewModel.sendSpecialKey("Ctrl+Z")
        viewModel.sendSpecialKey("Ctrl+D")
        viewModel.sendSpecialKey("Tab")
        viewModel.sendSpecialKey("Esc")

        // History navigation
        viewModel.onCommandInputChanged("cmd1")
        viewModel.sendCurrentCommand()
        viewModel.onCommandInputChanged("cmd2")
        viewModel.sendCurrentCommand()

        viewModel.sendSpecialKey("▲")
        assertEquals("cmd2", viewModel.commandInputText.value)

        viewModel.sendSpecialKey("▼")
        assertEquals("cmd2", viewModel.commandInputText.value)
    }

    @Test
    fun testFontSizeAdjustment() {
        val initialSize = viewModel.fontSizeSp.value
        viewModel.increaseFontSize()
        assertEquals(initialSize + 2, viewModel.fontSizeSp.value)

        viewModel.decreaseFontSize()
        assertEquals(initialSize, viewModel.fontSizeSp.value)
    }

    @Test
    fun testClearActiveTerminal() {
        viewModel.onCommandInputChanged("pwd")
        viewModel.sendCurrentCommand()

        viewModel.clearActiveTerminal()
        val activeTab = viewModel.getActiveTab()
        assertNotNull(activeTab)
        assertTrue(activeTab!!.lines.isEmpty())
    }

    @Test
    fun testDialogStateAndPersistence() {
        val profile = SshProfile(name = "Test Profile", host = "10.0.0.1", username = "user")

        viewModel.openProfileDialog(profile)
        assertTrue(viewModel.showProfileDialog.value)
        assertEquals(profile, viewModel.selectedProfileForEdit.value)

        viewModel.closeProfileDialog()
        assertFalse(viewModel.showProfileDialog.value)
        assertNull(viewModel.selectedProfileForEdit.value)

        viewModel.openSnippetDialog()
        assertTrue(viewModel.showSnippetDialog.value)
        viewModel.closeSnippetDialog()
        assertFalse(viewModel.showSnippetDialog.value)

        // Test saving/deleting
        val snippet = SavedSnippet(title = "Uptime", command = "uptime")
        viewModel.saveProfile(profile)
        viewModel.saveSnippet(snippet)
        viewModel.deleteProfile(profile)
        viewModel.deleteSnippet(snippet)
    }
}
