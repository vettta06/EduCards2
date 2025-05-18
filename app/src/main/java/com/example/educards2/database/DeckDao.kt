package com.example.educards2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Insert
    suspend fun insert(deck: Deck)

    @Update
    suspend fun update(deck: Deck)

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Long): Deck?

    @Query("SELECT * FROM decks WHERE isBuiltIn = 1")
    fun getAllBuiltInDecks(): Flow<List<Deck>>
}