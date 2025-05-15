package com.example.educards2.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats")
data class Stats(
    @PrimaryKey
    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "cards_solved")
    val cardsSolved: Int
)

data class DailyStat(
    val date: String,
    val count: Int
)

data class MonthlyStat(
    val month: String,
    val total: Int
)