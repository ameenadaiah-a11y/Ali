package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [SurahEntity::class, AyahEntity::class, DhikrEntity::class, WirdConfigEntity::class, WirdLogEntity::class], version = 2, exportSchema = false)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(QuranDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class QuranDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.quranDao())
                }
            }
        }

        private suspend fun populateDatabase(quranDao: QuranDao) {
            // Seed 114 Surahs metadata
            quranDao.insertSurahs(QuranDataHelper.getSurahsMetadata())
            // Seed custom verses for full readability of first few & auto-generation fallback!
            quranDao.insertAyahs(QuranDataHelper.getSeededAyahs())
            // Seed Dhikrs
            quranDao.insertDhikrs(QuranDataHelper.getSeededDhikrs())
        }
    }
}
