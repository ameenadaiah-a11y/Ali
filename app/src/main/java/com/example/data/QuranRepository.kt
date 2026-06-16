package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class QuranRepository(private val quranDao: QuranDao) {

    val allSurahs: Flow<List<SurahEntity>> = quranDao.getAllSurahs().flowOn(Dispatchers.IO)
    val allBookmarks: Flow<List<BookmarkEntity>> = quranDao.getAllBookmarks().flowOn(Dispatchers.IO)
    val allHifzPlans: Flow<List<HifzPlanEntity>> = quranDao.getAllHifzPlans().flowOn(Dispatchers.IO)
    val allNotes: Flow<List<NoteEntity>> = quranDao.getAllNotes().flowOn(Dispatchers.IO)
    val readingHistory: Flow<ReadingHistoryEntity?> = quranDao.getReadingHistory().flowOn(Dispatchers.IO)
    val recentStats: Flow<List<DailyStatsEntity>> = quranDao.getRecentStats().flowOn(Dispatchers.IO)

    fun getAyahsForSurah(surahNum: Int): Flow<List<AyahEntity>> =
        quranDao.getAyahsForSurah(surahNum).flowOn(Dispatchers.IO)

    fun getAyahsForPage(page: Int): Flow<List<AyahEntity>> =
        quranDao.getAyahsForPage(page).flowOn(Dispatchers.IO)

    fun getAyahsForJuz(juz: Int): Flow<List<AyahEntity>> =
        quranDao.getAyahsForJuz(juz).flowOn(Dispatchers.IO)

    fun isBookmarked(id: String): Flow<Boolean> = quranDao.isBookmarked(id).flowOn(Dispatchers.IO)

    fun getAdhkarByCategory(category: String): Flow<List<DhikrEntity>> =
        quranDao.getAdhkarByCategory(category).flowOn(Dispatchers.IO)

    fun getStatsForDate(date: String): Flow<DailyStatsEntity?> =
        quranDao.getStatsForDate(date).flowOn(Dispatchers.IO)

    suspend fun insertSurahs(surahs: List<SurahEntity>) = withContext(Dispatchers.IO) {
        quranDao.insertSurahs(surahs)
    }

    suspend fun insertAyahs(ayahs: List<AyahEntity>) = withContext(Dispatchers.IO) {
        quranDao.insertAyahs(ayahs)
    }

    suspend fun addBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        quranDao.insertBookmark(bookmark)
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        quranDao.deleteBookmark(bookmark)
    }

    suspend fun addHifzPlan(plan: HifzPlanEntity) = withContext(Dispatchers.IO) {
        quranDao.insertHifzPlan(plan)
    }

    suspend fun deleteHifzPlan(plan: HifzPlanEntity) = withContext(Dispatchers.IO) {
        quranDao.deleteHifzPlan(plan)
    }

    suspend fun insertAdhkar(adhkar: List<DhikrEntity>) = withContext(Dispatchers.IO) {
        quranDao.insertAdhkar(adhkar)
    }

    suspend fun addNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        quranDao.insertNote(note)
    }

    suspend fun deleteNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        quranDao.deleteNote(note)
    }

    suspend fun updateReadingHistory(history: ReadingHistoryEntity) = withContext(Dispatchers.IO) {
        quranDao.insertReadingHistory(history)
    }

    suspend fun saveStats(stats: DailyStatsEntity) = withContext(Dispatchers.IO) {
        quranDao.insertOrUpdateStats(stats)
    }

    suspend fun searchAyahs(query: String): List<AyahEntity> = withContext(Dispatchers.IO) {
        quranDao.searchAyahsSync(query)
    }

    // --- Gemini AI Assistant integration ---
    suspend fun askQuranAssistant(userPrompt: String): String = withContext(Dispatchers.IO) {
        var apiKey = ""
        try {
            // Retrieve key via BuildConfig safely
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            apiKey = field.get(null) as? String ?: ""
        } catch (e: Exception) {
            Log.w("QuranRepository", "GEMINI_API_KEY field not found in BuildConfig.")
        }

        if (apiKey.isBlank()) {
            return@withContext getOfflineAIAssistantResponse(userPrompt)
        }

        try {
            val systemInstruction = "أنت مساعد قرآني ذكي وخبير متخصص في علوم القرآن والتفسير واللغة العربية. تجيب باختصار وبأسلوب إسلامي راقٍ وأنيق وتستشهد بالآيات والقرآن الموثق."
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(userPrompt)))),
                systemInstruction = GeminiInstruction(parts = listOf(GeminiPart(systemInstruction)))
            )
            val response = GeminiClient.service.askGemini(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "عذراً، لم أستطع الحصول على إجابة في الوقت الحالي."
        } catch (e: Exception) {
            Log.e("QuranRepository", "Error calling Gemini API: ${e.message}", e)
            getOfflineAIAssistantResponse(userPrompt)
        }
    }

    private fun getOfflineAIAssistantResponse(prompt: String): String {
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("تفسير") || lowerPrompt.contains("فسر") -> {
                "التفسير المساعد: تفضل يا أخي القارئ باستخدام قسم التفسير الموجود أسفل كل آية في شاشة القراءة، حيث يوفر التطبيق تفاسير معتمدة (تفسير السعدي، تفسير ابن كثير، والتفسير الميسر) دون الحاجة للاتصال بالإنترنت."
            }
            lowerPrompt.contains("حفظ") || lowerPrompt.contains("مراجعة") -> {
                "نصيحة الحفظ: لتسهيل حفظ القرآن الكريم، ننصحك باستخدام قسم 'خطط الحفظ والمراجعة' المدمج في التطبيق، حيث يمكنك وضع خطة يومية مناسبة لجدولك ومتابعة تقدّمك مع إحصائيات دقيقة."
            }
            lowerPrompt.contains("ورد") || lowerPrompt.contains("يومي") -> {
                "الورد اليومي المقترح: ننصحك اليوم بقراءة 4 صفحات (صفحة 1 إلى 4) من سورة البقرة، مع الاستماع لتلاوة مباركة من القارئ المفضل لديك لتثبيت مواضع الكلمات ومخارج الحروف الصحيحة."
            }
            lowerPrompt.contains("سورة") -> {
                "هل تبحث عن تفاصيل سورة معينة؟ يمكنك تصفح الفهرس الشامل على الشاشة الرئيسية الذي يعرض أسماء السور، عدد آياتها، ومكان نزولها (مكية أو مدنية) مع محرك بحث ذكي وفوري."
            }
            else -> {
                "أنا المساعد القرآني الذكي. يبدو أنك تستخدم التطبيق في الوضع المحلي (دون تفعيل مفتاح Gemini API). تفضل بطرح أي سؤال وسأحاول إرشادك لأفضل الممارسات والأذكار والتلاوات المناسبة المتوفرة بالتطبيق!"
            }
        }
    }
}
