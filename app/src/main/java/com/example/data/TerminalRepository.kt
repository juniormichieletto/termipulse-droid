package com.example.data

import kotlinx.coroutines.flow.Flow

class TerminalRepository(
    private val profileDao: SshProfileDao,
    private val snippetDao: SavedSnippetDao
) {
    val allProfiles: Flow<List<SshProfile>> = profileDao.getAllProfiles()
    val allSnippets: Flow<List<SavedSnippet>> = snippetDao.getAllSnippets()

    suspend fun getProfileById(id: Long): SshProfile? = profileDao.getProfileById(id)

    suspend fun saveProfile(profile: SshProfile): Long {
        return if (profile.id == 0L) {
            profileDao.insertProfile(profile)
        } else {
            profileDao.updateProfile(profile)
            profile.id
        }
    }

    suspend fun deleteProfile(profile: SshProfile) {
        profileDao.deleteProfile(profile)
    }

    suspend fun deleteProfileById(id: Long) {
        profileDao.deleteProfileById(id)
    }

    suspend fun saveSnippet(snippet: SavedSnippet): Long {
        return if (snippet.id == 0L) {
            snippetDao.insertSnippet(snippet)
        } else {
            snippetDao.updateSnippet(snippet)
            snippet.id
        }
    }

    suspend fun deleteSnippet(snippet: SavedSnippet) {
        snippetDao.deleteSnippet(snippet)
    }
}
