package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val arabicName: String,
    val englishName: String,
    val revelationPlace: String, // "مكية" أو "مدنية"
    val totalVerses: Int
)

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey val id: String, // format: "surah:ayah"
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val tafsirSaadi: String,
    val tafsirKathir: String,
    val tafsirTabari: String,
    val isBookmarked: Boolean = false
)

@Entity(tableName = "dhikrs")
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "صباح", "مساء", "نوم", "بعد الصلاة"
    val text: String,
    val description: String,
    val targetCount: Int,
    val currentCount: Int = 0
)

@Entity(tableName = "wird_config")
data class WirdConfigEntity(
    @PrimaryKey val id: Int = 1,
    val targetDays: Int = 30,       // Estimated finish target (30, 60, 90 etc.)
    val currentPage: Int = 0,       // Current page successfully finished (0 to 604)
    val startDateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "wird_logs")
data class WirdLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String,         // "YYYY-MM-DD"
    val completedPage: Int,         // Ended at page
    val pagesReadCount: Int,        // Pages read in this interval
    val timestamp: Long = System.currentTimeMillis()
)
