package com.juniormichieletto

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.juniormichieletto.data.AppDatabase
import com.juniormichieletto.data.SavedSnippet
import com.juniormichieletto.data.SshProfile
import com.juniormichieletto.data.TerminalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TerminalDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: TerminalRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TerminalRepository(db.sshProfileDao(), db.savedSnippetDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testSshProfileEntityAndDao() = runBlocking {
        val profile = SshProfile(
            name = "Production Server",
            host = "192.168.1.100",
            port = 22,
            username = "admin",
            authType = "PASSWORD",
            passwordOrKey = "secret123",
            defaultDirectory = "/var/www",
            badgeColorHex = "#FF5252",
            isSandbox = false
        )

        val id = repository.saveProfile(profile)
        assertTrue(id > 0)

        val profiles = repository.allProfiles.first()
        assertEquals(1, profiles.size)
        val fetched = profiles[0]
        assertEquals("Production Server", fetched.name)
        assertEquals("192.168.1.100", fetched.host)
        assertEquals(22, fetched.port)
        assertEquals("admin", fetched.username)
        assertEquals("PASSWORD", fetched.authType)
        assertEquals("secret123", fetched.passwordOrKey)
        assertEquals("/var/www", fetched.defaultDirectory)
        assertEquals("#FF5252", fetched.badgeColorHex)

        val updatedProfile = fetched.copy(name = "Updated Server")
        repository.saveProfile(updatedProfile)

        val updatedList = repository.allProfiles.first()
        assertEquals("Updated Server", updatedList[0].name)

        repository.deleteProfile(updatedList[0])
        val emptyList = repository.allProfiles.first()
        assertTrue(emptyList.isEmpty())
    }

    @Test
    fun testSavedSnippetEntityAndDao() = runBlocking {
        val snippet = SavedSnippet(
            title = "Check Disk Space",
            command = "df -h",
            category = "SYSTEM"
        )

        val id = repository.saveSnippet(snippet)
        assertTrue(id > 0)

        val snippets = repository.allSnippets.first()
        assertEquals(1, snippets.size)
        val fetched = snippets[0]
        assertEquals("Check Disk Space", fetched.title)
        assertEquals("df -h", fetched.command)
        assertEquals("SYSTEM", fetched.category)

        val updated = fetched.copy(title = "Disk Check Updated")
        repository.saveSnippet(updated)

        val updatedList = repository.allSnippets.first()
        assertEquals("Disk Check Updated", updatedList[0].title)

        repository.deleteSnippet(updatedList[0])
        val emptyList = repository.allSnippets.first()
        assertTrue(emptyList.isEmpty())
    }
}
