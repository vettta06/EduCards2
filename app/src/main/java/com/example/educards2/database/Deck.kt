package com.example.educards2.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import com.example.educards2.R

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isBuiltIn: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val isLocked: Boolean = isBuiltIn
)