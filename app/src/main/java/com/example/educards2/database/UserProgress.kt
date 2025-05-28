package com.example.educards2.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserProgress(
    @PrimaryKey val cardId: Long,
    val interval: Long,
    val nextReview: Long,
    val eFactor: Double,
    val repetitionCount: Int
)