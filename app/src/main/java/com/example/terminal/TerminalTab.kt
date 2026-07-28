package com.example.terminal

import java.util.UUID

data class TerminalLine(
    val id: String = UUID.randomUUID().toString(),
    val rawText: String,
    val isInput: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class TerminalTab(
    val id: String = UUID.randomUUID().toString(),
    val profileId: Long? = null,
    val title: String,
    val hostLabel: String,
    val username: String,
    val host: String,
    val port: Int = 22,
    val badgeColorHex: String = "#00E676",
    val isConnected: Boolean = true,
    val isConnecting: Boolean = false,
    val isLongJobRunning: Boolean = false,
    val statusMessage: String = "Connected",
    val currentDirectory: String = "~",
    val lines: List<TerminalLine> = emptyList(),
    val commandHistory: List<String> = emptyList(),
    val isSandbox: Boolean = false
)
