package com.juniormichieletto.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedSnippetDao {
    @Query("SELECT * FROM saved_snippets ORDER BY title ASC")
    fun getAllSnippets(): Flow<List<SavedSnippet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SavedSnippet): Long

    @Update
    suspend fun updateSnippet(snippet: SavedSnippet)

    @Delete
    suspend fun deleteSnippet(snippet: SavedSnippet)
}
