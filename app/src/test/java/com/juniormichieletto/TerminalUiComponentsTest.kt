package com.juniormichieletto

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.juniormichieletto.data.SavedSnippet
import com.juniormichieletto.data.SshProfile
import com.juniormichieletto.terminal.TerminalLine
import com.juniormichieletto.terminal.TerminalTab
import com.juniormichieletto.ui.terminal.CommandInputBar
import com.juniormichieletto.ui.terminal.ControlKeyBar
import com.juniormichieletto.ui.terminal.ProfileManagerDialog
import com.juniormichieletto.ui.terminal.SnippetManagerSheet
import com.juniormichieletto.ui.terminal.TerminalCanvasView
import com.juniormichieletto.ui.terminal.TerminalTabBar
import com.juniormichieletto.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TerminalUiComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCommandInputBar() {
        var textValue = ""
        var sendClicked = false
        var snippetsClicked = false
        var historyUpClicked = false
        var historyDownClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                CommandInputBar(
                    inputText = textValue,
                    onInputChanged = { textValue = it },
                    onSend = { sendClicked = true },
                    onOpenSnippetsClick = { snippetsClicked = true },
                    onHistoryUp = { historyUpClicked = true },
                    onHistoryDown = { historyDownClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("command_input_field").performTextInput("ls -la")
        assertEquals("ls -la", textValue)

        composeTestRule.onNodeWithTag("send_command_button").performClick()
        assertTrue(sendClicked)

        composeTestRule.onNodeWithTag("snippets_drawer_button").performClick()
        assertTrue(snippetsClicked)

        composeTestRule.onNodeWithContentDescription("Previous Command").performClick()
        assertTrue(historyUpClicked)

        composeTestRule.onNodeWithContentDescription("Next Command").performClick()
        assertTrue(historyDownClicked)
    }

    @Test
    fun testControlKeyBar() {
        var clickedKey = ""

        composeTestRule.setContent {
            MyApplicationTheme {
                ControlKeyBar(
                    isCtrlActive = false,
                    isAltActive = false,
                    onToggleCtrl = { clickedKey = "CTRL" },
                    onToggleAlt = { clickedKey = "ALT" },
                    onKeyClick = { clickedKey = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Ctrl").performClick()
        assertEquals("CTRL", clickedKey)

        composeTestRule.onNodeWithText("Alt").performClick()
        assertEquals("ALT", clickedKey)

        composeTestRule.onNodeWithText("Tab").performClick()
        assertEquals("Tab", clickedKey)

        composeTestRule.onNodeWithText("Esc").performClick()
        assertEquals("Esc", clickedKey)
    }

    @Test
    fun testTerminalTabBar() {
        val tab1 = TerminalTab(id = "1", title = "Tab 1", hostLabel = "localhost", username = "user", host = "127.0.0.1")
        val tab2 = TerminalTab(id = "2", title = "Tab 2", hostLabel = "server", username = "root", host = "10.0.0.1")

        val selectedIndex = androidx.compose.runtime.mutableStateOf(0)
        var closedIndex = -1
        var addTabClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                TerminalTabBar(
                    tabs = listOf(tab1, tab2),
                    activeTabIndex = selectedIndex.value,
                    onTabSelected = { selectedIndex.value = it },
                    onTabClosed = { closedIndex = it },
                    onAddTabClick = { addTabClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Tab 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tab 2").assertIsDisplayed()

        composeTestRule.onNodeWithText("Tab 2").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, selectedIndex.value)

        composeTestRule.onNodeWithTag("add_tab_button").performClick()
        assertTrue(addTabClicked)
    }

    @Test
    fun testTerminalCanvasView() {
        val tab = TerminalTab(
            id = "1",
            title = "Sandbox",
            hostLabel = "localhost",
            username = "termipulse",
            host = "127.0.0.1",
            lines = listOf(
                TerminalLine(rawText = "Welcome to TermiPulse"),
                TerminalLine(rawText = "echo Hello World")
            )
        )

        var zoomIn = false
        var zoomOut = false
        var cleared = false

        composeTestRule.setContent {
            MyApplicationTheme {
                TerminalCanvasView(
                    activeTab = tab,
                    fontSizeSp = 14,
                    onIncreaseFont = { zoomIn = true },
                    onDecreaseFont = { zoomOut = true },
                    onClearTerminal = { cleared = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Welcome to TermiPulse").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Zoom In").performClick()
        assertTrue(zoomIn)

        composeTestRule.onNodeWithContentDescription("Zoom Out").performClick()
        assertTrue(zoomOut)

        composeTestRule.onNodeWithContentDescription("Clear Output").performClick()
        assertTrue(cleared)
    }

    @Test
    fun testProfileManagerDialog() {
        val profiles = listOf(
            SshProfile(id = 1, name = "Server A", host = "10.0.0.1", username = "root")
        )

        var savedProfile: SshProfile? = null
        var deletedProfile: SshProfile? = null
        var selectedProfile: SshProfile? = null
        var dismissed = false

        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileManagerDialog(
                    profiles = profiles,
                    profileToEdit = null,
                    onConnect = { selectedProfile = it },
                    onSaveProfile = { savedProfile = it },
                    onDeleteProfile = { deletedProfile = it },
                    onDismiss = { dismissed = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Server A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Server A").performClick()
        assertEquals(profiles[0], selectedProfile)
    }

    @Test
    fun testSnippetManagerSheet() {
        val snippets = listOf(
            SavedSnippet(id = 1, title = "System Uptime", command = "uptime", category = "MONITORING")
        )

        var executedSnippet: SavedSnippet? = null
        var savedSnippet: SavedSnippet? = null
        var dismissed = false

        composeTestRule.setContent {
            MyApplicationTheme {
                SnippetManagerSheet(
                    snippets = snippets,
                    onDismiss = { dismissed = true },
                    onExecuteSnippet = { executedSnippet = it },
                    onSaveSnippet = { savedSnippet = it },
                    onDeleteSnippet = {}
                )
            }
        }

        composeTestRule.onNodeWithText("System Uptime").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Run Command").performClick()
        assertEquals(snippets[0], executedSnippet)
    }
}
