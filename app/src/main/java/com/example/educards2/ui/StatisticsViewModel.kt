package com.example.educards2.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educards2.database.DailyStat
import com.example.educards2.database.MonthlyStat
import com.example.educards2.database.StatsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.withContext

class StatisticsViewModel(private val statsDao: StatsDao) : ViewModel() {
    private val _dailyStats = mutableStateOf<List<DailyStat>>(emptyList())
    val dailyStats: List<DailyStat> get() = _dailyStats.value

    private val _monthlyStats = mutableStateOf<List<MonthlyStat>>(emptyList())
    val monthlyStats: List<MonthlyStat> get() = _monthlyStats.value

    private val _totalCards = mutableStateOf(0)
    val totalCards: Int get() = _totalCards.value

    private val _isLoading = mutableStateOf(true)
    val isLoading: Boolean get() = _isLoading.value

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val daily = statsDao.getStatsBetweenDates(
                    getFormattedDate(calendar.time),
                    getFormattedDate(Date())
                )
                val monthly = statsDao.getYearlyStats()

                withContext(Dispatchers.Main) {
                    _dailyStats.value = fillMissingDays(daily)
                    _monthlyStats.value = monthly
                    _totalCards.value = statsDao.getTotalCardsSolved() ?: 0
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
                e.printStackTrace()
            }
        }
    }
    private fun fillMissingDays(stats: List<DailyStat>): List<DailyStat> {
        val filled = mutableListOf<DailyStat>()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val existingStats = stats.associateBy { it.date }

        repeat(30) {
            val date = getFormattedDate(calendar.time)
            filled.add(existingStats[date] ?: DailyStat(date, 0))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return filled
    }
    private fun getFormattedDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }
}
