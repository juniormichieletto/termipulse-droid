package com.juniormichieletto

import com.juniormichieletto.terminal.TerminalLine
import com.juniormichieletto.terminal.TerminalTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalTabTest {

    @Test
    fun testTerminalLineDefaults() {
        val line = TerminalLine(rawText = "ls -la")
        assertNotNull(line.id)
        assertEquals("ls -la", line.rawText)
        assertFalse(line.isInput)
        assertTrue(line.timestamp > 0)
    }

    @Test
    fun testTerminalLineCustom() {
        val line = TerminalLine(id = "123", rawText = "pwd", isInput = true, timestamp = 1000L)
        assertEquals("123", line.id)
        assertEquals("pwd", line.rawText)
        assertTrue(line.isInput)
        assertEquals(1000L, line.timestamp)
    }

    @Test
    fun testTerminalTabDefaults() {
        val tab = TerminalTab(
            title = "Sandbox",
            hostLabel = "localhost",
            username = "user",
            host = "127.0.0.1"
        )
        assertNotNull(tab.id)
        assertNull(tab.profileId)
        assertEquals("Sandbox", tab.title)
        assertEquals("localhost", tab.hostLabel)
        assertEquals("user", tab.username)
        assertEquals("127.0.0.1", tab.host)
        assertEquals(22, tab.port)
        assertEquals("#00E676", tab.badgeColorHex)
        assertTrue(tab.isConnected)
        assertFalse(tab.isConnecting)
        assertFalse(tab.isLongJobRunning)
        assertEquals("Connected", tab.statusMessage)
        assertEquals("~", tab.currentDirectory)
        assertTrue(tab.lines.isEmpty())
        assertTrue(tab.commandHistory.isEmpty())
        assertFalse(tab.isSandbox)
    }

    @Test
    fun testTerminalTabCopy() {
        val tab = TerminalTab(
            title = "Original",
            hostLabel = "host",
            username = "admin",
            host = "10.0.0.1"
        )

        val updated = tab.copy(
            isConnected = false,
            statusMessage = "Disconnected",
            currentDirectory = "/var/log",
            isSandbox = true,
            lines = listOf(TerminalLine(rawText = "line 1"))
        )

        assertEquals("Original", updated.title)
        assertFalse(updated.isConnected)
        assertEquals("Disconnected", updated.statusMessage)
        assertEquals("/var/log", updated.currentDirectory)
        assertTrue(updated.isSandbox)
        assertEquals(1, updated.lines.size)
        assertEquals("line 1", updated.lines[0].rawText)
    }
}
