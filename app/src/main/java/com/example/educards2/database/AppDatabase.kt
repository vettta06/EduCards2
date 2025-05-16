package com.example.educards2.database

import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.util.Log
import androidx.room.Database
import com.example.educards2.R
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [Card::class, Stats::class, Deck::class],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun statsDao(): StatsDao
    abstract fun deckDao(): DeckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS stats (" +
                            "date TEXT PRIMARY KEY NOT NULL, " +
                            "cards_solved INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN rating INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN eFactor REAL NOT NULL DEFAULT 2.5"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN nextReviewDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}"
                )
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN intervalStep INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN currentInterval INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE new_cards (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        question TEXT NOT NULL,
                        answer TEXT NOT NULL,
                        rating INTEGER NOT NULL DEFAULT 0,
                        eFactor REAL NOT NULL DEFAULT 2.5,
                        nextReviewDate INTEGER NOT NULL,
                        currentInterval INTEGER NOT NULL DEFAULT 0,
                        isBuiltIn INTEGER NOT NULL DEFAULT 0,  
                        isArchived INTEGER NOT NULL DEFAULT 0 
                    )
                """)

                database.execSQL("""
                    INSERT INTO new_cards 
                    (id, question, answer, rating, eFactor, nextReviewDate, currentInterval)
                    SELECT 
                    id, question, answer, rating, eFactor, nextReviewDate, currentInterval 
                    FROM cards
                """)

                database.execSQL("DROP TABLE cards")
                database.execSQL("ALTER TABLE new_cards RENAME TO cards")
            }
        }
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS decks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        isBuiltIn INTEGER NOT NULL DEFAULT 0,
                        iconResId INTEGER NOT NULL DEFAULT 0,
                        createdDate INTEGER NOT NULL
                    )
                    """
                )
                database.execSQL(
                    "ALTER TABLE cards ADD COLUMN deckId INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
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
                                val cards = getDefaultCards(context)
                                getDatabase(context).cardDao().insertAll(cards)
                                Log.d("DB", "Добавлено ${cards.size} карточек")
                            }
                        }
                    })
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun getDefaultCards(context: Context): List<Card> {
            val linearAlgebraDeck = Deck(
                name = "Линейная алгебра",
                description = "Основы линейной алгебры",
                isBuiltIn = true,
                iconResId = R.drawable.ic_math
            )

            GlobalScope.launch(Dispatchers.IO) {
                INSTANCE?.deckDao()?.insert(linearAlgebraDeck)
            }

            return listOf(
                Card(
                    deckId = 1,
                    question = "Что такое ранг матрицы?",
                    answer = "Максимальное число линейно независимых строк или столбцов",
                    isBuiltIn = true
                ),
            )
        }
        private fun initializeBuiltInDecks(db: AppDatabase, context: Context) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val decks = db.deckDao().getAllBuiltInDecks()

                    if (decks.isEmpty()) {
                        val builtInDecks = listOf(
                            Deck(
                                name = "Математика",
                                description = "Основные математические понятия",
                                isBuiltIn = true,
                                iconResId = R.drawable.ic_math
                            ),
                            Deck(
                                name = "Программирование",
                                description = "Основы программирования",
                                isBuiltIn = true,
                                iconResId = R.drawable.ic_code
                            )
                        )
                        builtInDecks.forEach { db.deckDao().insert(it) }
                    }
                } catch (e: Exception) {
                    Log.e("AppDatabase", "Error initializing decks", e)
                }
            }
        }
    }
}