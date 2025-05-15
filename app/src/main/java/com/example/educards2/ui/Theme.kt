package com.example.educards2.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EduCardsColorScheme = lightColorScheme(
    primary = Color(0xDE6A8321),
    background = Color(0xFFF2EBE5),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onBackground = Color(0xFF474a51),
    onSurface = Color(0xFF000000),
    onTertiary = Color(0xFF000000),
)
@Composable
fun EduCardsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EduCardsColorScheme,
        content = content
    )
}
