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
    entities = [Card::class, Stats::class, Deck::class, UserProgress::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun statsDao(): StatsDao
    abstract fun deckDao(): DeckDao
    abstract fun userProgressDao(): UserDao

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
                    /*.addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val cards = getDefaultCards()
                                getDatabase(context).cardDao().insertAll(cards)
                            }
                        }
                    })*/
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}