package com.example.educards2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(progress: UserProgress)

    @Query("SELECT * FROM userprogress")
    suspend fun getAll(): List<UserProgress>

    @Query("SELECT * FROM userprogress WHERE cardId = :cardId")
    suspend fun getByCardId(cardId: Long): UserProgress?
}