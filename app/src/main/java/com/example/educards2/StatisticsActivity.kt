package com.example.educards2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educards2.database.AppDatabase
import com.example.educards2.ui.EduCardsTheme
import com.example.educards2.ui.StatisticsScreen
import com.example.educards2.ui.StatisticsViewModel
import com.example.educards2.ui.StatisticsViewModelFactory

class StatisticsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EduCardsTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val statsDao = AppDatabase.getDatabase(this).statsDao()

                    val viewModel: StatisticsViewModel = viewModel(
                        factory = StatisticsViewModelFactory(statsDao)
                    )
                    StatisticsScreen(
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}