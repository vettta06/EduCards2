package com.example.educards2.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CardDao {
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
    suspend fun archiveCard(cardId: Long)

    @Query("UPDATE cards SET isArchived = 0 WHERE id = :cardId")
    suspend fun restoreCard(cardId: Long)  // Changed from Int to Long for consistency

    @Query("SELECT * FROM cards WHERE isArchived = 1")
    fun getArchivedCards(): Flow<List<Card>>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND isArchived = 0")
    suspend fun getTotalCardsInDeck(deckId: Long): Int

    // Keep only one version of getDueCardsCount using Long timestamp
    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND nextReview <= :nowTimestamp")
    suspend fun getDueCardsCount(deckId: Long, nowTimestamp: Long = System.currentTimeMillis()): Int

    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    fun getCardsByDeck(deckId: Long): Flow<List<Card>>


    @Query("SELECT * FROM cards WHERE deckId = :deckId AND isArchived = 0 AND (nextReview <= :currentTime OR nextReview IS NULL)")
    fun getDueCardsByDeck(deckId: Long, currentTime: Long = System.currentTimeMillis()): Flow<List<Card>>
}