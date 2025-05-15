package com.example.educards2.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao{
    @Insert
    suspend fun insert(card: Card)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<Card>)
    @Update
    suspend fun update(card: Card)

    @Delete
    suspend fun delete(card: Card)

    @Query("SELECT * FROM cards ORDER BY id DESC")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT * FROM cards")
    suspend fun getAllCardsSync(): List<Card>

    @Query("UPDATE cards SET isArchived = 1 WHERE id = :cardId")
    suspend fun archiveCard(cardId: Int)

    @Query("UPDATE cards SET isArchived = 0 WHERE id = :cardId")
    suspend fun restoreCard(cardId: Int)

    @Query("SELECT * FROM cards WHERE isArchived = 1")
    fun getArchivedCards(): Flow<List<Card>>

}