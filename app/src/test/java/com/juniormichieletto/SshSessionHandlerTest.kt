package com.juniormichieletto

import com.juniormichieletto.data.SshProfile
import com.juniormichieletto.terminal.SessionEvent
import com.juniormichieletto.terminal.SshSessionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SshSessionHandlerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val sandboxProfile = SshProfile(
        id = 1,
        name = "Local Sandbox",
        host = "localhost",
        port = 22,
        username = "termipulse",
        authType = "NONE",
        passwordOrKey = "",
        defaultDirectory = "~",
        badgeColorHex = "#00E676",
        isSandbox = true
    )

    @Test
    fun testConnectSandboxEmitsBannerAndPrompt() = testScope.runTest {
        val handler = SshSessionHandler("tab-1", sandboxProfile, this)

        val events = mutableListOf<SessionEvent>()
        val job = launch(testDispatcher) {
            handler.events.collect { events.add(it) }
        }

        handler.connect()
        advanceTimeBy(500)

        assertTrue(handler.isConnected)
        assertTrue(events.isNotEmpty())
        assertTrue(events.any { it is SessionEvent.StatusChanged && it.isConnected })
        assertTrue(events.any { it is SessionEvent.OutputReceived && it.text.contains("TermiPulse Local Terminal Sandbox") })

        handler.disconnect()
        assertFalse(handler.isConnected)
        job.cancel()
    }

    @Test
    fun testSandboxCommands() = testScope.runTest {
        val handler = SshSessionHandler("tab-1", sandboxProfile, this)

        val events = mutableListOf<SessionEvent>()
        val job = launch(testDispatcher) {
            handler.events.collect { events.add(it) }
        }

        handler.connect()
        advanceTimeBy(400)
        events.clear()

        // Test commands
        val commands = listOf(
            "help", "pwd", "cd var", "cd ..", "whoami", "uname", "date",
            "uptime", "ls", "top", "docker ps", "docker", "df", "git",
            "clear", "echo Hello World", "nonexistent_cmd"
        )

        for (cmd in commands) {
            handler.sendCommand(cmd)
            advanceTimeBy(100)
        }

        assertTrue(events.isNotEmpty())
        assertTrue(events.any { it is SessionEvent.OutputReceived && it.text.contains("TermiPulse Built-in Commands") })
        assertTrue(events.any { it is SessionEvent.OutputReceived && it.text.contains("Hello World") })
        assertTrue(events.any { it is SessionEvent.OutputReceived && it.text.contains("command not found") })

        job.cancel()
    }

    @Test
    fun testLongJobAndCancelJob() = testScope.runTest {
        val handler = SshSessionHandler("tab-1", sandboxProfile, this)

        val events = mutableListOf<SessionEvent>()
        val job = launch(testDispatcher) {
            handler.events.collect { events.add(it) }
        }

        handler.connect()
        advanceTimeBy(400)
        events.clear()

        // Start long job
        handler.sendCommand("stress-test")
        advanceTimeBy(200)

        assertTrue(handler.isLongJobRunning)

        // Cancel job
        handler.sendCommand("cancel-job")
        advanceTimeBy(200)

        assertFalse(handler.isLongJobRunning)
        assertTrue(events.any { it is SessionEvent.OutputReceived && it.text.contains("JOB CANCELLED") })

        job.cancel()
    }

    @Test
    fun testSendCtrlC() = testScope.runTest {
        val handler = SshSessionHandler("tab-1", sandboxProfile, this)

        val events = mutableListOf<SessionEvent>()
        val job = launch(testDispatcher) {
            handler.events.collect { events.add(it) }
        }

        handler.connect()
        advanceTimeBy(400)
        events.clear()

        handler.sendCommand("build-job")
        advanceTimeBy(200)
        assertTrue(handler.isLongJobRunning)

        handler.sendCtrlC()
        advanceTimeBy(200)
        assertFalse(handler.isLongJobRunning)
        assertTrue(events.any { it is SessionEvent.OutputReceived && it.text.contains("SIGINT") })

        job.cancel()
    }
}
