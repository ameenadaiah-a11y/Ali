package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val number: Int,
    val name: String,
    val englishName: String,
    val arabicName: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val startPage: Int,
    val juzNumber: Int
)

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey val id: String, // format "surah_ayah"
    val surahNumber: Int,
    val ayahNumber: Int,
    val textArabic: String,
    val textEnglish: String,
    val page: Int,
    val juz: Int,
    val hizb: Int,
    val tafsirSaadi: String = "",
    val tafsirKathir: String = "",
    val tafsirMuyassar: String = ""
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // format "surah_ayah" or "page_X"
    val surahNumber: Int,
    val ayahNumber: Int,
    val isPageBookmark: Boolean,
    val pageNumber: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "hifz_plans")
data class HifzPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int,
    val surahName: String,
    val startAyah: Int,
    val endAyah: Int,
    val targetDays: Int,
    val completedDays: Int = 0,
    val progressPercent: Float = 0f,
    val completed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "adhkar")
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "morning", "evening", "sleep", "prayer", "wake_up"
    val content: String,
    val count: Int,
    val description: String = ""
)

@Entity(tableName = "verse_notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int,
    val surahName: String = "",
    val ayahNumber: Int,
    val noteContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: String, // Format: YYYY-MM-DD
    val pagesRead: Int = 0,
    val surahsRead: Int = 0,
    val readingDurationMinutes: Int = 0,
    val appOpenCount: Int = 0
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val id: Int = 1, // keeping only 1 active record of last read position
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val pageNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)
