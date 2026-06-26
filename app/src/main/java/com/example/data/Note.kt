package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val updatedAt: Long = System.currentTimeMillis(),
    val colorHex: String? = null,
    val isPinned: Boolean = false,
    val topic: String = "默认",
    val showInWidget: Boolean = true
)
