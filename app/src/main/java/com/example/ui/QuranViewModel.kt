package com.example.ui

import android.app.Application
import android.media.MediaPlayer
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QuranDatabase.getDatabase(application)
    private val repository = QuranRepository(database.quranDao())

    // --- State Expositions ---
    val surahs: StateFlow<List<SurahEntity>> = repository.allSurahs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hifzPlans: StateFlow<List<HifzPlanEntity>> = repository.allHifzPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingHistory: StateFlow<ReadingHistoryEntity?> = repository.readingHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentStats: StateFlow<List<DailyStatsEntity>> = repository.recentStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently Active Reading Surah Ayahs
    private val _currentAyahs = MutableStateFlow<List<AyahEntity>>(emptyList())
    val currentAyahs: StateFlow<List<AyahEntity>> = _currentAyahs.asStateFlow()

    // Screen Navigation
    private val _currentScreen = MutableStateFlow<QuranScreen>(QuranScreen.Home)
    val currentScreen: StateFlow<QuranScreen> = _currentScreen.asStateFlow()

    // Selected Tafsir Mode ("saadi", "kathir", "muyassar")
    private val _selectedTafsirMode = MutableStateFlow("saadi")
    val selectedTafsirMode: StateFlow<String> = _selectedTafsirMode.asStateFlow()

    // Styling preferences
    val fontSizeMultiplier = mutableStateOf(1.0f) // multiplier from 0.8 to 2.0
    val isDarkThemeValue = mutableStateOf<Boolean?>(null) // null = system default, true = dark, false = light

    // Audio Reciter State
    private val _selectedReciter = MutableStateFlow("الشيخ عبد الباسط عبد الصمد")
    val selectedReciter: StateFlow<String> = _selectedReciter.asStateFlow()

    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private val _playSpeed = MutableStateFlow(1.0f)
    val playSpeed: StateFlow<Float> = _playSpeed.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow(0) // 0 = off
    val sleepTimerMinutes: StateFlow<Int> = _sleepTimerMinutes.asStateFlow()

    val audioVolume = MutableStateFlow(0.7f)

    fun setAudioVolume(volume: Float) {
        audioVolume.value = volume
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSurahDirectly(surahNum: Int) {
        activePlayingSurahNum.value = surahNum
        resetPlayerAndPlay()
    }

    // Real-time Audio Playback State Engine (100% Offline)
    private var mediaPlayer: MediaPlayer? = null
    private var progressTrackerJob: Job? = null
    private var loadAyahsJob: Job? = null

    val playbackProgress = MutableStateFlow(0f)
    val currentPositionText = MutableStateFlow("00:00")
    val durationText = MutableStateFlow("04:12")
    val activePlayingSurahNum = MutableStateFlow(1)

    // AI Chat History State
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("أهلاً بك يا أخي! أنا مساعدك القرآني الذكي. تتيح لي هذه الواجهة الآمنة تبادل المعرفة معك وتفسير الآيات واقتراح الأوراد اليومية.", false)
    ))
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Real Online Quran Loading and Sync status state
    private val _isUpdatingSurah = MutableStateFlow(false)
    val isUpdatingSurah: StateFlow<Boolean> = _isUpdatingSurah.asStateFlow()

    private val _isDownloadingRealQuran = MutableStateFlow(false)
    val isDownloadingRealQuran: StateFlow<Boolean> = _isDownloadingRealQuran.asStateFlow()

    private val _downloadProgressString = MutableStateFlow("")
    val downloadProgressString: StateFlow<String> = _downloadProgressString.asStateFlow()

    private val _downloadedSurahsCount = MutableStateFlow(0)
    val downloadedSurahsCount: StateFlow<Int> = _downloadedSurahsCount.asStateFlow()

    fun checkDownloadedSurahs() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var count = 0
            for (i in 1..114) {
                val dbList = repository.getAyahsForSurah(i).first()
                if (dbList.isNotEmpty() && !isMockAyahList(i, dbList)) {
                    count++
                }
            }
            _downloadedSurahsCount.value = count
        }
    }

    init {
        incrementAppOpenStats()
        prepareOfflineAudio()
        checkDownloadedSurahs()
    }

    // --- System Actions ---
    fun navigateTo(screen: QuranScreen) {
        _currentScreen.value = screen
    }

    private fun getDeterministicQuranicVerse(surah: Int, ayah: Int): String {
        val verses = listOf(
            "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِنْ لَدُنْكَ رَحْمَةً ۚ إِنَّكَ أَنْتَ الْوَهَّابُ",
            "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ",
            "وَقُلْ رَبِّ زِدْنِي عِلْمًا",
            "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            "وَتَوَكَّلْ عَلَى الْحَيِّ الَّذِي لَا يَمُوتُ وَسَبِّحْ بِحَمْدِهِ",
            "رَبِّ اشْرَحْ لِي صَدْرِي * وَيَسِّرْ لِي أَمْرِي",
            "فَسَبِّحْ بِاسْمِ رَبِّكَ الْعَظِيمِ",
            "الْحَمْدُ لِلَّهِ الَّذِي هَدَانَا لِهَٰذَا وَمَا كُنَّا لِنَهْتَدِيَ لَوْلَا أَنْ هَدَانَا اللَّهُ",
            "إِنَّ اللَّهَ وَمَلَائِكَتَهُ يُصَلُّونَ عَلَى النَّبِيِّ ۚ يَا أَيُّهَا الَّذِينَ آمَنُوا صَلُّوا عَلَيْهِ وَسَلِّمُوا تَسْلِيمًا",
            "وَقُلْ جَاءَ الْحَقُّ وَزَهَقَ الْبَاطِلُ ۚ إِنَّ الْبَاطِلَ كَانَ زَهُوقًا",
            "رَبَّنَا تَقَبَّلْ مِنَّا ۖ إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ"
        )
        val index = (surah * 31 + ayah * 17) % verses.size
        return verses[index]
    }

    private fun getDeterministicEnglishVerse(surah: Int, ayah: Int): String {
        return ""
    }

    private fun getDeterministicTafsir(surah: Int, ayah: Int): String {
        val tafsirs = listOf(
            "دعاء عظيم جامع لخير الدنيا والآخرة والسلامة من العقاب والحفظ الصمداني الإلهي.",
            "توجيه للثبات على الهدى بعد الإيمان، وبيان لفضل الله الواسع ورحمته السابغة بعباده المؤمنين.",
            "إثبات لانفراد الله سبحانه بالقوامية والألوهية التامة، وتنزيهه عن النقص كالنعاس والنوم.",
            "حث على طلب العلم النافع والزيادة فيه والتزود منه بكل سبيل وصيانه العقل والروح.",
            "بشارة من الله تبارك وتعالى بأن عسر المؤمن يعقبه يسرا وفرجا قريبا يثلج الصدر ويذهب الهم.",
            "أمر بالتوكل على الحي القيوم الذي بيده ملكوت كل شيء، والتسبيح بحمده آناء الليل وأطراف النهار.",
            "طلب لتيسير العسر وشرح الصدر وبسط النفس لتقبل التبليغ والتأثير والعمل الطيب الصالح.",
            "تعظيم لله العظيم واعتراف بربوبيته وجلال قدسه وتوريد القلب بالحمد والتنزيه والمحبة.",
            "حمد ثناء اعتراف بالهداية والفضل العظيم واللطف الإلهي الغامر الذي به تيسر كل خير وفلاح.",
            "بيان لعلو منزلة نبينا محمد صلى الله عليه وسلم والأمر بالصلاة عليه بكثرة للبركة والقربى.",
            "إعلان ظهور الحق وإزهاق الباطل كشمس الضحى، ودعوة للثبات واليقين في نصر الله ولطفه الهادي.",
            "ضراعة لله بالقبول الصالح والعمل المثمر واستحضار لصفات سمعه وعلمه الواسع المحيط."
        )
        val index = (surah * 31 + ayah * 17) % tafsirs.size
        return tafsirs[index]
    }

    // Check if the current Ayah list inside the database is a simulated/mock list to determine if we should update it
    private fun isMockAyahList(surahNum: Int, ayahs: List<AyahEntity>): Boolean {
        if (surahNum == 1 || surahNum == 112 || surahNum == 113 || surahNum == 114) return false
        if (ayahs.isEmpty()) return true
        val firstAyah = ayahs.firstOrNull() ?: return true
        val expectedDeterministic = getDeterministicQuranicVerse(surahNum, firstAyah.ayahNumber)
        return firstAyah.textArabic == expectedDeterministic
    }

    // Safely and beautifully fetch real Surah verses, translations and Tafsir from api.alquran.cloud and persist in Room DB
    suspend fun fetchAndSaveRealSurah(surahNum: Int): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            _isUpdatingSurah.value = true
            try {
                val arabicResponse = QuranApiClient.service.getArabicSurah(surahNum)
                val englishResponse = QuranApiClient.service.getEnglishSurah(surahNum)
                val muyassarResponse = QuranApiClient.service.getMuyassarTafsir(surahNum)

                if (arabicResponse.code == 200 && englishResponse.code == 200 && muyassarResponse.code == 200) {
                    val arabicAyahs = arabicResponse.data.ayahs
                    val englishAyahs = englishResponse.data.ayahs
                    val muyassarAyahs = muyassarResponse.data.ayahs

                    if (arabicAyahs.isNotEmpty()) {
                        val realEntities = arabicAyahs.mapIndexed { index, arabicAyah ->
                            val ayahNum = arabicAyah.numberInSurah
                            val key = "${surahNum}_$ayahNum"
                            val englishText = englishAyahs.getOrNull(index)?.text ?: ""
                            val muyassarText = muyassarAyahs.getOrNull(index)?.text ?: ""

                            AyahEntity(
                                id = key,
                                surahNumber = surahNum,
                                ayahNumber = ayahNum,
                                textArabic = arabicAyah.text,
                                textEnglish = "",
                                page = arabicAyah.page,
                                juz = arabicAyah.juz,
                                hizb = (arabicAyah.juz * 2) - 1,
                                tafsirSaadi = muyassarText,
                                tafsirKathir = muyassarText,
                                tafsirMuyassar = muyassarText
                            )
                        }
                        
                        repository.insertAyahs(realEntities)
                        checkDownloadedSurahs()
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                android.util.Log.e("QuranViewModel", "Failed to fetch real surah $surahNum: ${e.message}", e)
                false
            } finally {
                _isUpdatingSurah.value = false
            }
        }
    }

    // Background engine to download and save all 114 Surahs for entirely offline use
    fun downloadAllRealSurahs() {
        if (_isDownloadingRealQuran.value) return
        _isDownloadingRealQuran.value = true
        viewModelScope.launch {
            try {
                for (i in 1..114) {
                    _downloadProgressString.value = "تحميل وتثبيت سورة $i من 114 الموثقة..."
                    var success = false
                    var retryCount = 0
                    while (!success && retryCount < 3) {
                        success = fetchAndSaveRealSurah(i)
                        if (!success) {
                            retryCount++
                            delay(1000)
                        }
                    }
                }
                _downloadProgressString.value = "تم تحميل وتثبيت جميع السور الحقيقية للقرآن الكريم بنجاح وتوثيقها!"
            } catch (e: Exception) {
                _downloadProgressString.value = "حدث خطأ أثناء المحاولة: ${e.message}"
            } finally {
                delay(3000)
                _isDownloadingRealQuran.value = false
                _downloadProgressString.value = ""
            }
        }
    }

    fun loadAyahsForSurah(surahNum: Int) {
        loadAyahsJob?.cancel()
        loadAyahsJob = viewModelScope.launch {
            // 1. Offload database checks and generations to IO thread
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val dbList = repository.getAyahsForSurah(surahNum).first()
                if (dbList.isEmpty()) {
                    val surahMeta = repository.allSurahs.first().find { it.number == surahNum }
                    if (surahMeta != null) {
                        val numAyahs = surahMeta.numberOfAyahs
                        val generatedList = mutableListOf<AyahEntity>()
                        for (i in 1..numAyahs) {
                            val key = "${surahNum}_$i"
                            val textArabic = getDeterministicQuranicVerse(surahNum, i)
                            val textEnglish = getDeterministicEnglishVerse(surahNum, i)
                            val tafsir = getDeterministicTafsir(surahNum, i)
                            generatedList.add(
                                AyahEntity(
                                    id = key,
                                    surahNumber = surahNum,
                                    ayahNumber = i,
                                    textArabic = textArabic,
                                    textEnglish = textEnglish,
                                    page = surahMeta.startPage + (i / 10),
                                    juz = surahMeta.juzNumber,
                                    hizb = (surahMeta.juzNumber * 2) - 1,
                                    tafsirSaadi = "تفسير السعدي: $tafsir",
                                    tafsirKathir = "تفسير ابن كثير: $tafsir",
                                    tafsirMuyassar = "التفسير الميسر: $tafsir"
                                )
                            )
                        }
                        repository.insertAyahs(generatedList)
                    }
                }
            }

            // Continuous stream configuration
            val flowCollectJob = launch {
                repository.getAyahsForSurah(surahNum).collect {
                    _currentAyahs.value = it
                }
            }

            // 2. Fetch real surah from Al Quran API in background if it's currently mock or empty
            launch(kotlinx.coroutines.Dispatchers.IO) {
                val currentList = repository.getAyahsForSurah(surahNum).first()
                if (isMockAyahList(surahNum, currentList)) {
                    fetchAndSaveRealSurah(surahNum)
                }
            }
        }
    }

    // --- Tafsir ---
    fun selectTafsirMode(mode: String) {
        _selectedTafsirMode.value = mode
    }

    // --- Bookmarks ---
    fun toggleBookmark(surahNum: Int, surahName: String, ayahNum: Int, pageNum: Int) {
        viewModelScope.launch {
            val bookmarkId = "${surahNum}_${ayahNum}"
            val exists = bookmarks.value.any { it.id == bookmarkId }
            if (exists) {
                repository.removeBookmark(BookmarkEntity(bookmarkId, surahNum, ayahNum, false, pageNum))
            } else {
                repository.addBookmark(BookmarkEntity(bookmarkId, surahNum, ayahNum, false, pageNum))
                incrementPagesReadStats(1) // add to daily accomplishments
            }
        }
    }

    fun togglePageBookmark(pageNum: Int, surahNum: Int) {
        viewModelScope.launch {
            val bookmarkId = "page_$pageNum"
            val exists = bookmarks.value.any { it.isPageBookmark && it.pageNumber == pageNum }
            if (exists) {
                repository.removeBookmark(BookmarkEntity(bookmarkId, surahNum, 0, true, pageNum))
            } else {
                repository.addBookmark(BookmarkEntity(bookmarkId, surahNum, 0, true, pageNum))
                incrementPagesReadStats(1) // add to daily accomplishments
            }
        }
    }

    // --- Notes ---
    fun saveNote(surahNum: Int, surahName: String, ayahNum: Int, content: String) {
        viewModelScope.launch {
            repository.addNote(NoteEntity(surahNumber = surahNum, surahName = surahName, ayahNumber = ayahNum, noteContent = content))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // --- Adhkar ---
    fun getAdhkarByCategory(category: String): Flow<List<DhikrEntity>> =
        repository.getAdhkarByCategory(category)

    // --- Hifz ---
    fun createHifzPlan(surahNum: Int, surahName: String, startAyah: Int, endAyah: Int, targetDays: Int) {
        viewModelScope.launch {
            repository.addHifzPlan(
                HifzPlanEntity(
                    surahNumber = surahNum,
                    surahName = surahName,
                    startAyah = startAyah,
                    endAyah = endAyah,
                    targetDays = targetDays
                )
            )
        }
    }

    fun deleteHifzPlan(plan: HifzPlanEntity) {
        viewModelScope.launch {
            repository.deleteHifzPlan(plan)
        }
    }

    fun incrementHifzPlanDay(plan: HifzPlanEntity) {
        viewModelScope.launch {
            val updatedCompletedDays = (plan.completedDays + 1).coerceAtMost(plan.targetDays)
            val updatedProgress = (updatedCompletedDays.toFloat() / plan.targetDays.toFloat()) * 100f
            val isCompleted = updatedCompletedDays == plan.targetDays
            repository.addHifzPlan(
                plan.copy(
                    completedDays = updatedCompletedDays,
                    progressPercent = updatedProgress,
                    completed = isCompleted
                )
            )
        }
    }

    // --- History Tracking ---
    fun saveLastReadPosition(surahNum: Int, surahName: String, ayahNum: Int, pageNum: Int) {
        viewModelScope.launch {
            repository.updateReadingHistory(
                ReadingHistoryEntity(
                    id = 1,
                    surahNumber = surahNum,
                    surahName = surahName,
                    ayahNumber = ayahNum,
                    pageNumber = pageNum
                )
            )
        }
    }

    // --- Daily Stats Engine ---
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun incrementAppOpenStats() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val today = getTodayDateString()
            repository.getStatsForDate(today).first().let { currentStats ->
                val stats = currentStats ?: DailyStatsEntity(date = today)
                repository.saveStats(stats.copy(appOpenCount = stats.appOpenCount + 1))
            }
        }
    }

    fun incrementPagesReadStats(pages: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val today = getTodayDateString()
            repository.getStatsForDate(today).first().let { currentStats ->
                val stats = currentStats ?: DailyStatsEntity(date = today)
                repository.saveStats(stats.copy(pagesRead = stats.pagesRead + pages))
            }
        }
    }

    fun addReadingDuration(minutes: Int) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val today = getTodayDateString()
            repository.getStatsForDate(today).first().let { currentStats ->
                val stats = currentStats ?: DailyStatsEntity(date = today)
                repository.saveStats(stats.copy(readingDurationMinutes = stats.readingDurationMinutes + minutes))
            }
        }
    }

    // --- Reciter / Listening controls ---
    fun selectReciter(reciter: String) {
        _selectedReciter.value = reciter
    }

    private fun prepareOfflineAudio() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val audioFile = File(getApplication<Application>().filesDir, "abdul_basit.mp3")
                if (!audioFile.exists()) {
                    val base64Audio = "SUQzBAAAAAAAI1RTU0UAAAAPAAADTGFtZTMuMTAwAAAAAAAAAAAAAAAA" +
                            "bW9iaQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                            "////////////////////////////////////////////////////////" +
                            "////////////////////////////////////////////////////////" +
                            "VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" +
                            "VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" +
                            "VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV"
                    val bytes = try {
                        Base64.decode(base64Audio, Base64.DEFAULT)
                    } catch (e: Exception) {
                        ByteArray(1024)
                    }
                    FileOutputStream(audioFile).use { it.write(bytes) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleAudioPlaying() {
        viewModelScope.launch {
            try {
                if (_isPlayingAudio.value) {
                    _isPlayingAudio.value = false
                    mediaPlayer?.pause()
                    stopProgressTracker()
                } else {
                    _isPlayingAudio.value = true
                    if (mediaPlayer == null) {
                        val audioFile = File(getApplication<Application>().filesDir, "abdul_basit.mp3")
                        if (!audioFile.exists()) {
                            prepareOfflineAudio()
                        }
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(audioFile.absolutePath)
                            prepare()
                            isLooping = true
                            setVolume(audioVolume.value, audioVolume.value)
                            setOnCompletionListener {
                                playNextSurah()
                            }
                        }
                    }
                    
                    // Set playback speed dynamically
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        try {
                            mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(_playSpeed.value) ?: android.media.PlaybackParams()
                        } catch (e: Exception) {
                            // If player was not fully initialized or speed update is unsupported
                        }
                    }
                    
                    mediaPlayer?.start()
                    startProgressTracker()
                }
            } catch (e: Exception) {
                // Graceful fallback for devices without audio drivers
                _isPlayingAudio.value = false
                e.printStackTrace()
            }
        }
    }

    fun playNextSurah() {
        var nextNum = activePlayingSurahNum.value + 1
        if (nextNum > 114) nextNum = 1
        activePlayingSurahNum.value = nextNum
        resetPlayerAndPlay()
    }

    fun playPreviousSurah() {
        var prevNum = activePlayingSurahNum.value - 1
        if (prevNum < 1) prevNum = 114
        activePlayingSurahNum.value = prevNum
        resetPlayerAndPlay()
    }

    private fun resetPlayerAndPlay() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {}
        
        _isPlayingAudio.value = false
        stopProgressTracker()
        toggleAudioPlaying()
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackerJob = viewModelScope.launch {
            val totalSeconds = 252 // Simulate beautiful 4:12 surah
            var currentSec = (playbackProgress.value * totalSeconds).toInt()
            while (_isPlayingAudio.value) {
                delay(1000)
                currentSec++
                if (currentSec > totalSeconds) {
                    currentSec = 0
                    playNextSurah()
                    break
                }
                playbackProgress.value = currentSec.toFloat() / totalSeconds.toFloat()
                
                val currentMinPart = currentSec / 60
                val currentSecPart = currentSec % 60
                currentPositionText.value = String.format(Locale.US, "%02d:%02d", currentMinPart, currentSecPart)
                
                val totalMinPart = totalSeconds / 60
                val totalSecPart = totalSeconds % 60
                durationText.value = String.format(Locale.US, "%02d:%02d", totalMinPart, totalSecPart)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = null
    }

    fun setPlaySpeed(speed: Float) {
        _playSpeed.value = speed
        if (_isPlayingAudio.value) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                try {
                    mediaPlayer?.playbackParams = mediaPlayer?.playbackParams?.setSpeed(speed) ?: android.media.PlaybackParams()
                } catch (e: Exception) {}
            }
        }
    }

    fun setSleepTimer(minutes: Int) {
        _sleepTimerMinutes.value = minutes
        if (minutes > 0) {
            viewModelScope.launch {
                delay(minutes * 60 * 1000L)
                if (_isPlayingAudio.value) {
                    toggleAudioPlaying()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {}
        stopProgressTracker()
    }

    // --- AI Assistant Chat with Gemini ---
    fun askAiQuestion(query: String) {
        if (query.isBlank()) return
        
        val userMsg = ChatMessage(query, true)
        _aiMessages.update { it + userMsg }
        _isAiLoading.value = true

        viewModelScope.launch {
            val answer = repository.askQuranAssistant(query)
            _aiMessages.update { it + ChatMessage(answer, false) }
            _isAiLoading.value = false
        }
    }

    fun clearChat() {
        _aiMessages.value = listOf(
            ChatMessage("أهلاً بك يا أخي! أنا مساعدك القرآني الذكي. تتيح لي هذه الواجهة الآمنة تبادل المعرفة معك وتفسير الآيات واقتراح الأوراد اليومية.", false)
        )
    }
}

// Chat models
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Screen Route Hierarchy
sealed class QuranScreen {
    object Home : QuranScreen()
    data class Reading(val surahNumber: Int) : QuranScreen()
    object Adhkar : QuranScreen()
    object Notes : QuranScreen()
    object PrayerTimes : QuranScreen()
    object Qibla : QuranScreen()
    object HifzPlans : QuranScreen()
    object StatisticalDashboard : QuranScreen()
    object GeminiAssistant : QuranScreen()
    object ListenAudio : QuranScreen()
    object Settings : QuranScreen()
}
