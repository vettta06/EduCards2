package com.example.educards2.database

data class Achievement(
    val title: String,
    val description: String,
    val iconRes: Int,
    var unlocked: Boolean
)