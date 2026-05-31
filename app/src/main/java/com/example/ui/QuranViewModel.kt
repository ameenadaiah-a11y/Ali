package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuranViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    // Database & Repository initialization
    private val database = QuranDatabase.getDatabase(application, viewModelScope)
    private val repository = QuranRepository(database.quranDao(), viewModelScope)
    
    // Audio Player initialization
    val audioPlayer = QuranAudioPlayer(application)

    // Sensor Manager for Compass
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var magneticSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // Compass angles
    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth

    // Active Screen state (Simple dynamic routing instead of heavy navigation setup)
    private val _activeScreenState = MutableStateFlow<QuranScreen>(QuranScreen.Home)
    val activeScreenState: StateFlow<QuranScreen> = _activeScreenState

    // Searching and filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Surahs listing
    val surahs: StateFlow<List<SurahEntity>> = repository.allSurahs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered surah list based on search bar
    val filteredSurahs: StateFlow<List<SurahEntity>> = combine(surahs, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.arabicName.contains(query) || 
                it.englishName.contains(query, ignoreCase = true) ||
                it.number.toString() == query
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active reading Surah and its Verses
    private val _selectedSurah = MutableStateFlow<SurahEntity?>(null)
    val selectedSurah: StateFlow<SurahEntity?> = _selectedSurah

    val activeAyahs: StateFlow<List<AyahEntity>> = _selectedSurah
        .flatMapLatest { surah ->
            if (surah != null) {
                repository.getAyahsForSurah(surah.number)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Bookmarks
    val bookmarkedAyahs: StateFlow<List<AyahEntity>> = repository.bookmarkedAyahs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    private val _fontSize = MutableStateFlow(24f) // Sp units for Quran texts
    val fontSize: StateFlow<Float> = _fontSize

    private val _isDarkMode = MutableStateFlow(true) // Default to dark green luxury mode
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _selectedTafsirType = MutableStateFlow("السعدي") // "السعدي" / "ابن كثير" / "الطبري"
    val selectedTafsirType: StateFlow<String> = _selectedTafsirType

    private val _selectedPlayReciter = MutableStateFlow(ReciterConfig.list[0])
    val selectedPlayReciter: StateFlow<Reciter> = _selectedPlayReciter

    // Adhkar category and values
    private val _selectedDhikrCategory = MutableStateFlow("صباح")
    val selectedDhikrCategory: StateFlow<String> = _selectedDhikrCategory

    val activeDhikrs: StateFlow<List<DhikrEntity>> = _selectedDhikrCategory
        .flatMapLatest { cat ->
            repository.getDhikrsByCategory(cat)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasbih counter
    private val _tasbihCount = MutableStateFlow(0)
    val tasbihCount: StateFlow<Int> = _tasbihCount

    // Prayertimes and location config
    private val _selectedCityIndex = MutableStateFlow(0)
    val selectedCityIndex: StateFlow<Int> = _selectedCityIndex

    val activePrayerTimes: StateFlow<PrayerTimes> = _selectedCityIndex
        .map { index ->
            val city = PrayerTimesCalculator.predefinedCities[index]
            PrayerTimesCalculator.calculateTimes(city.latitude, city.longitude, city.timezone, city.name)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 
            PrayerTimesCalculator.calculateTimes(21.4225, 39.8262, 3.0, "مكة المكرمة")
        )

    val qiblaAngle: StateFlow<Double> = _selectedCityIndex
        .map { index ->
            val city = PrayerTimesCalculator.predefinedCities[index]
            PrayerTimesCalculator.getQiblaAngle(city.latitude, city.longitude)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Last read bookmark auto-saving context
    private val _lastReadSurahNum = MutableStateFlow(1)
    val lastReadSurahNum: StateFlow<Int> = _lastReadSurahNum

    private val _lastReadAyahNum = MutableStateFlow(1)
    val lastReadAyahNum: StateFlow<Int> = _lastReadAyahNum

    // Keyword search over all Quran ayahs
    private val _allQuranSearchQuery = MutableStateFlow("")
    val allQuranSearchQuery: StateFlow<String> = _allQuranSearchQuery

    val searchResults: StateFlow<List<AyahEntity>> = _allQuranSearchQuery
        .debounce(400)
        .flatMapLatest { query ->
            if (query.length < 2) {
                flowOf(emptyList())
            } else {
                repository.searchAyahs(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wird Tracking Shared State
    val wirdConfig: StateFlow<WirdConfigEntity?> = repository.wirdConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wirdLogs: StateFlow<List<WirdLogEntity>> = repository.allWirdLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Register sensors
        registerSensors()
        
        // Load initial settings if stored, or set defaults
        viewModelScope.launch {
            // Check if databases are ready, trigger pre-pop callback
            // Flow will automatically pick it up
        }
    }

    private fun registerSensors() {
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Fallback to magnetic & accelerometer
            sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        audioPlayer.stopPlayback()
    }

    // Navigation and Routing Actions
    fun navigateTo(screen: QuranScreen) {
        _activeScreenState.value = screen
    }

    fun selectSurah(surah: SurahEntity) {
        _selectedSurah.value = surah
        _activeScreenState.value = QuranScreen.Reading(surah)
        // Auto-save last read
        saveLastRead(surah.number, 1)
    }

    fun saveLastRead(surahNum: Int, ayahNum: Int) {
        _lastReadSurahNum.value = surahNum
        _lastReadAyahNum.value = ayahNum
    }

    // Setters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateAllQuranSearchQuery(query: String) {
        _allQuranSearchQuery.value = query
    }

    fun setFontSize(size: Float) {
        _fontSize.value = size.coerceIn(16f, 48f)
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setTafsirType(type: String) {
        _selectedTafsirType.value = type
    }

    fun selectReciter(reciter: Reciter) {
        _selectedPlayReciter.value = reciter
    }

    fun selectDhikrCategory(category: String) {
        _selectedDhikrCategory.value = category
    }

    fun setCityIndex(index: Int) {
        _selectedCityIndex.value = index
    }

    // Database Actions
    fun toggleBookmarkAyah(ayah: AyahEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(ayah.surahNumber, ayah.ayahNumber, !ayah.isBookmarked)
        }
    }

    fun incrementDhikr(dhikr: DhikrEntity) {
        viewModelScope.launch {
            if (dhikr.currentCount < dhikr.targetCount) {
                repository.updateDhikrCount(dhikr.id, dhikr.currentCount + 1)
            }
        }
    }

    fun resetDhikr(category: String) {
        viewModelScope.launch {
            repository.resetDhikrCountsByCategory(category)
        }
    }

    // Tasbeeh clicks
    fun clickTasbih() {
        _tasbihCount.value += 1
    }

    fun resetTasbih() {
        _tasbihCount.value = 0
    }

    // Wird Tracker Actions
    fun setWirdGoal(targetDays: Int) {
        viewModelScope.launch {
            val existing = repository.wirdConfig.first() ?: WirdConfigEntity()
            repository.saveWirdConfig(existing.copy(targetDays = targetDays))
        }
    }

    fun logWirdProgress(completedPageValue: Int, dateStr: String) {
        viewModelScope.launch {
            val current = repository.wirdConfig.first() ?: WirdConfigEntity()
            val prevPage = current.currentPage
            val readCount = (completedPageValue - prevPage).coerceAtLeast(0)
            
            // Log this session
            repository.insertWirdLog(
                WirdLogEntity(
                    dateString = dateStr,
                    completedPage = completedPageValue,
                    pagesReadCount = if (prevPage == 0) completedPageValue else readCount
                )
            )
            // Update current page
            repository.saveWirdConfig(current.copy(currentPage = completedPageValue))
        }
    }

    fun resetWirdTracker() {
        viewModelScope.launch {
            repository.clearWirdLogs()
            val current = repository.wirdConfig.first() ?: WirdConfigEntity()
            repository.saveWirdConfig(current.copy(currentPage = 0))
        }
    }

    // SensorEventListener overrides for visual compass pointer
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent?) {
        val ev = event ?: return
        if (ev.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, ev.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthRad = orientation[0]
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            _azimuth.value = (azimuthDeg + 360f) % 360f
        } else {
            if (ev.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity = ev.values.clone()
            }
            if (ev.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic = ev.values.clone()
            }
            if (gravity.isNotEmpty() && geomagnetic.isNotEmpty()) {
                val r = FloatArray(9)
                val i = FloatArray(9)
                if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(r, orientation)
                    val azimuthRad = orientation[0]
                    val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                    _azimuth.value = (azimuthDeg + 360f) % 360f
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

sealed class QuranScreen {
    object Home : QuranScreen()
    data class Reading(val surah: SurahEntity) : QuranScreen()
    object AudioPlayerScreen : QuranScreen()
    object Adhkar : QuranScreen()
    object PrayerAndCompass : QuranScreen()
    object SearchScreen : QuranScreen()
    object BookmarksScreen : QuranScreen()
    object Settings : QuranScreen()
    object WirdScreen : QuranScreen()
}
