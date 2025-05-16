package com.example.educards2.database

import java.util.Date

data class DeckProgress(
    val deckId: Long,
    val lastReviewed: Date,
    val totalCards: Int,
    val masteredCards: Int
)
