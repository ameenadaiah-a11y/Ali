package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class QuranRepository(
    private val quranDao: QuranDao,
    private val externalScope: CoroutineScope
) {
    val allSurahs: Flow<List<SurahEntity>> = quranDao.getAllSurahs()

    val bookmarkedAyahs: Flow<List<AyahEntity>> = quranDao.getBookmarkedAyahs()

    fun getAyahsForSurah(surahNum: Int): Flow<List<AyahEntity>> = flow {
        // First, check what's in the database
        val dbAyahs = quranDao.getAyahsForSurah(surahNum).first()
        if (dbAyahs.isNotEmpty()) {
            emit(dbAyahs)
        } else {
            // No ayahs seeded for this surah yet. Fetch Surah metadata
            val surah = quranDao.getSurahByNumber(surahNum)
            if (surah != null) {
                // Generate the verses dynamically
                val generated = QuranDataHelper.generateAyahsForSurah(surah)
                // Insert them in background so they are persisted forever
                externalScope.launch(Dispatchers.IO) {
                    quranDao.insertAyahs(generated)
                }
                emit(generated)
            } else {
                emit(emptyList())
            }
        }
        
        // Then continuously emit updates from database
        quranDao.getAyahsForSurah(surahNum).collect {
            emit(it)
        }
    }.flowOn(Dispatchers.IO)

    fun searchAyahs(query: String): Flow<List<AyahEntity>> {
        return quranDao.searchAyahs(query).flowOn(Dispatchers.IO)
    }

    suspend fun toggleBookmark(surahNum: Int, ayahNum: Int, isBookmarked: Boolean) {
        quranDao.updateBookmark(surahNum, ayahNum, isBookmarked)
    }

    fun getDhikrsByCategory(category: String): Flow<List<DhikrEntity>> {
        return quranDao.getDhikrsByCategory(category).flowOn(Dispatchers.IO)
    }

    suspend fun updateDhikrCount(id: Int, count: Int) {
        quranDao.updateDhikrCount(id, count)
    }

    suspend fun resetDhikrCountsByCategory(category: String) {
        quranDao.resetDhikrCountsByCategory(category)
    }

    // --- Wird Features Repository Methods ---
    val wirdConfig: Flow<WirdConfigEntity?> = quranDao.getWirdConfigFlow()
    val allWirdLogs: Flow<List<WirdLogEntity>> = quranDao.getAllWirdLogsFlow()

    suspend fun saveWirdConfig(config: WirdConfigEntity) {
        quranDao.insertOrUpdateWirdConfig(config)
    }

    suspend fun insertWirdLog(log: WirdLogEntity) {
        quranDao.insertWirdLog(log)
    }

    suspend fun updateWirdPage(page: Int) {
        quranDao.updateWirdCurrentPage(page)
    }

    suspend fun clearWirdLogs() {
        quranDao.clearWirdLogs()
    }
}
