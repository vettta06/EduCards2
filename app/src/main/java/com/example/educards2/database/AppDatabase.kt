package com.example.educards2.database

import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [Card::class, Stats::class, Deck::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun statsDao(): StatsDao
    abstract fun deckDao(): DeckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cards_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val cards = getDefaultCards()
                                getDatabase(context).cardDao().insertAll(cards)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getDefaultCards(): List<Card> {
            val linearAlgebraDeck = Deck(
                id = 1,
                name = "Линейная алгебра",
                description = "Основы линейной алгебры",
                isBuiltIn = true
            )

            val mathAnalysisDeck = Deck(
                id = 2,
                name = "Математический анализ",
                description = "Основы матанализа",
                isBuiltIn = true
            )

            val oopDeck = Deck(
                id = 3,
                name = "ООП",
                description = "Объектно-ориентированное программирование",
                isBuiltIn = true
            )

            GlobalScope.launch(Dispatchers.IO) {
                INSTANCE?.deckDao()?.apply {
                    insert(linearAlgebraDeck)
                    insert(mathAnalysisDeck)
                    insert(oopDeck)
                }
            }

            return listOf(
                Card(
                    deckId = 1,
                    question = "Что такое ранг матрицы?",
                    answer = "Максимальное число линейно независимых строк или столбцов",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 1,
                    question = "Как проверить, что векторы ортогональны?",
                    answer = "Их скалярное произведение равно нулю",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 1,
                    question = "Что означает det(A) = 0?",
                    answer = "Матрица A вырожденная (не имеет обратной)",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 2,
                    question = "Что такое производная?",
                    answer = "Скорость изменения функции в точке",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Как найти предел функции?",
                    answer = "Подставить точку, если неопределённость — упростить или использовать правило Лопиталя",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Что вычисляет интеграл?",
                    answer = "Площадь под кривой или первообразную функции",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 3,
                    question = "Что такое класс?",
                    answer = "Шаблон для создания объектов",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое объект?",
                    answer = "Экземпляр класса",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое функция?",
                    answer = "Блок кода, который выполняет действие",
                    isBuiltIn = true
                )
            )
        }
    }
}