package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ssh_profiles")
data class SshProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: String = "PASSWORD", // "PASSWORD" or "KEY"
    val passwordOrKey: String = "",
    val badgeColorHex: String = "#00E676",
    val defaultDirectory: String = "~",
    val isSandbox: Boolean = false,
    val lastConnected: Long = System.currentTimeMillis()
)
