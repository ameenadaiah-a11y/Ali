package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    // --- Surahs ---
    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    // --- Ayahs ---
    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNum ORDER BY ayahNumber ASC")
    fun getAyahsForSurah(surahNum: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE page = :page ORDER BY surahNumber ASC, ayahNumber ASC")
    fun getAyahsForPage(page: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE juz = :juz ORDER BY surahNumber ASC, ayahNumber ASC")
    fun getAyahsForJuz(juz: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE textArabic LIKE '%' || :query || '%' ORDER BY surahNumber ASC, ayahNumber ASC")
    suspend fun searchAyahsSync(query: String): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    // --- Bookmarks ---
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun isBookmarked(id: String): Flow<Boolean>

    // --- Hifz Plans ---
    @Query("SELECT * FROM hifz_plans ORDER BY timestamp DESC")
    fun getAllHifzPlans(): Flow<List<HifzPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHifzPlan(plan: HifzPlanEntity)

    @Delete
    suspend fun deleteHifzPlan(plan: HifzPlanEntity)

    // --- Adhkar ---
    @Query("SELECT * FROM adhkar WHERE category = :category ORDER BY id ASC")
    fun getAdhkarByCategory(category: String): Flow<List<DhikrEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdhkar(adhkar: List<DhikrEntity>)

    // --- Notes ---
    @Query("SELECT * FROM verse_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    // --- Daily Stats ---
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    fun getStatsForDate(date: String): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats ORDER BY date DESC LIMIT 7")
    fun getRecentStats(): Flow<List<DailyStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: DailyStatsEntity)

    // --- Reading History ---
    @Query("SELECT * FROM reading_history WHERE id = 1")
    fun getReadingHistory(): Flow<ReadingHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingHistory(history: ReadingHistoryEntity)
}
