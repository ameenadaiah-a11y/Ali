package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    @Query("SELECT * FROM surahs ORDER BY number ASC")
    fun getAllSurahs(): Flow<List<SurahEntity>>

    @Query("SELECT * FROM surahs WHERE number = :surahNum LIMIT 1")
    suspend fun getSurahByNumber(surahNum: Int): SurahEntity?

    @Query("SELECT * FROM ayahs WHERE surahNumber = :surahNum ORDER BY ayahNumber ASC")
    fun getAyahsForSurah(surahNum: Int): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE isBookmarked = 1")
    fun getBookmarkedAyahs(): Flow<List<AyahEntity>>

    @Query("SELECT * FROM ayahs WHERE textArabic LIKE '%' || :query || '%'")
    fun searchAyahs(query: String): Flow<List<AyahEntity>>

    @Query("UPDATE ayahs SET isBookmarked = :isBookmarked WHERE surahNumber = :surahNum AND ayahNumber = :ayahNum")
    suspend fun updateBookmark(surahNum: Int, ayahNum: Int, isBookmarked: Boolean)

    @Query("SELECT * FROM dhikrs WHERE category = :category")
    fun getDhikrsByCategory(category: String): Flow<List<DhikrEntity>>

    @Query("UPDATE dhikrs SET currentCount = :count WHERE id = :id")
    suspend fun updateDhikrCount(id: Int, count: Int)

    @Query("UPDATE dhikrs SET currentCount = 0 WHERE category = :category")
    suspend fun resetDhikrCountsByCategory(category: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<SurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<AyahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDhikrs(dhikrs: List<DhikrEntity>)

    // --- Wird Features DAO ---
    @Query("SELECT * FROM wird_config WHERE id = 1 LIMIT 1")
    fun getWirdConfigFlow(): Flow<WirdConfigEntity?>

    @Query("SELECT * FROM wird_config WHERE id = 1 LIMIT 1")
    suspend fun getWirdConfig(): WirdConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWirdConfig(config: WirdConfigEntity)

    @Query("SELECT * FROM wird_logs ORDER BY timestamp DESC")
    fun getAllWirdLogsFlow(): Flow<List<WirdLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWirdLog(log: WirdLogEntity)

    @Query("DELETE FROM wird_logs")
    suspend fun clearWirdLogs()

    @Query("UPDATE wird_config SET currentPage = :page WHERE id = 1")
    suspend fun updateWirdCurrentPage(page: Int)
}
