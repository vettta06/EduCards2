package com.example.educards2.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StatsDao {
    @Query("SELECT date, cards_solved AS count FROM stats WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getStatsBetweenDates(startDate: String, endDate: String): List<DailyStat>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: Stats)

    @Query("SELECT SUM(cards_solved) FROM stats")
    suspend fun getTotalCardsSolved(): Int?

    @Query("SELECT COUNT(*) FROM stats WHERE cards_solved > 0")
    suspend fun getTotalDaysWithActivity(): Int

    @Query("""
        SELECT strftime('%Y-%m', date) AS month, 
               SUM(cards_solved) AS total 
        FROM stats 
        GROUP BY month 
        ORDER BY month DESC 
        LIMIT 12
    """)
    suspend fun getYearlyStats(): List<MonthlyStat>
    @Query("SELECT * FROM stats WHERE date = :date LIMIT 1")
    suspend fun getStatsByDate(date: String): Stats?
}