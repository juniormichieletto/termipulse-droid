package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_snippets")
data class SavedSnippet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val command: String,
    val category: String = "General",
    val description: String = ""
)
