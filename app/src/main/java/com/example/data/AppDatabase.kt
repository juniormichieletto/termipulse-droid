package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SshProfile::class, SavedSnippet::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sshProfileDao(): SshProfileDao
    abstract fun savedSnippetDao(): SavedSnippetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "termipulse_db"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.sshProfileDao(), database.savedSnippetDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(profileDao: SshProfileDao, snippetDao: SavedSnippetDao) {
            // Default Profiles
            profileDao.insertProfile(
                SshProfile(
                    name = "Local Terminal Sandbox",
                    host = "127.0.0.1",
                    port = 22,
                    username = "termipulse",
                    authType = "PASSWORD",
                    badgeColorHex = "#00E676",
                    isSandbox = true,
                    lastConnected = System.currentTimeMillis()
                )
            )

            profileDao.insertProfile(
                SshProfile(
                    name = "Demo Ubuntu Cloud Instance",
                    host = "ubuntu-server.demo.net",
                    port = 22,
                    username = "ubuntu",
                    authType = "PASSWORD",
                    badgeColorHex = "#00E5FF",
                    isSandbox = false,
                    lastConnected = System.currentTimeMillis() - 3600000
                )
            )

            profileDao.insertProfile(
                SshProfile(
                    name = "Raspberry Pi Homelab",
                    host = "192.168.1.105",
                    port = 22,
                    username = "pi",
                    authType = "PASSWORD",
                    badgeColorHex = "#FFC107",
                    isSandbox = false,
                    lastConnected = System.currentTimeMillis() - 86400000
                )
            )

            // Default Snippets
            snippetDao.insertSnippet(
                SavedSnippet(
                    title = "System Metrics (htop)",
                    command = "htop",
                    category = "Monitoring",
                    description = "Live CPU, memory, and task manager"
                )
            )
            snippetDao.insertSnippet(
                SavedSnippet(
                    title = "Docker Status",
                    command = "docker ps -a",
                    category = "Containers",
                    description = "List all running and stopped docker containers"
                )
            )
            snippetDao.insertSnippet(
                SavedSnippet(
                    title = "Disk Usage",
                    command = "df -h",
                    category = "System",
                    description = "Check filesystem disk space consumption"
                )
            )
            snippetDao.insertSnippet(
                SavedSnippet(
                    title = "Git Status",
                    command = "git status",
                    category = "Dev",
                    description = "Show working directory git state"
                )
            )
            snippetDao.insertSnippet(
                SavedSnippet(
                    title = "Tail Logs",
                    command = "tail -f /var/log/syslog",
                    category = "Monitoring",
                    description = "Real-time system log stream"
                )
            )
            snippetDao.insertSnippet(
                SavedSnippet(
                    title = "Long Job Test",
                    command = "stress-test",
                    category = "Demo",
                    description = "Simulate a background build task with real-time progress"
                )
            )
        }
    }
}
