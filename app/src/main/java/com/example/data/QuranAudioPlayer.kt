package com.example.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

sealed class AudioState {
    object Idle : AudioState()
    object Loading : AudioState()
    data class Playing(val surahNumber: Int, val reciterName: String, val progress: Float, val timeText: String) : AudioState()
    data class Paused(val surahNumber: Int, val reciterName: String, val progress: Float, val timeText: String) : AudioState()
    data class Error(val message: String) : AudioState()
}

data class Reciter(
    val id: String,
    val name: String,
    val baseUrl: String
)

object ReciterConfig {
    val list = listOf(
        Reciter("afs", "مشاري العفاسي", "https://server8.mp3quran.net/afs/"),
        Reciter("basit", "عبد الباسط عبد الصمد", "https://server7.mp3quran.net/basit/"),
        Reciter("maher", "ماهر المعيقلي", "https://server12.mp3quran.net/maher/"),
        Reciter("s_gmd", "سعد الغامدي", "https://server7.mp3quran.net/s_gmd/"),
        Reciter("yasser", "ياسر الدوسري", "https://server11.mp3quran.net/yasser/")
    )
}

class QuranAudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    
    private val _audioState = MutableStateFlow<AudioState>(AudioState.Idle)
    val audioState: StateFlow<AudioState> = _audioState

    // Track simulated downloading status
    private val _downloadedSurahs = MutableStateFlow<Set<String>>(emptySet())
    val downloadedSurahs: StateFlow<Set<String>> = _downloadedSurahs

    private var activeSurah: Int = -1
    private var activeReciter: Reciter = ReciterConfig.list[0]
    private var updateThread: Thread? = null

    init {
        // Load simulated index from cache if needed
        _downloadedSurahs.value = setOf("1_afs", "112_afs", "113_afs", "114_afs") // pre-downloaded Al-Fatihah, Al-Ikhlas, etc.
    }

    fun playSurah(surahNum: Int, reciter: Reciter) {
        stopPlayback()
        _audioState.value = AudioState.Loading
        activeSurah = surahNum
        activeReciter = reciter

        val fileName = String.format("%03d.mp3", surahNum)
        val urlString = reciter.baseUrl + fileName

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(urlString))
                setOnPreparedListener { mp ->
                    mp.start()
                    startProgressTracker()
                }
                setOnCompletionListener {
                    stopProgressTracker()
                    _audioState.value = AudioState.Idle
                }
                setOnErrorListener { _, what, extra ->
                    _audioState.value = AudioState.Error("فشل تحميل الصوت. تأكد من اتصالك بالإنترنت.")
                    stopProgressTracker()
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _audioState.value = AudioState.Error("خطأ غير متوقع: ${e.localizedMessage}")
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        val surah = activeSurah
        val reciter = activeReciter

        if (player.isPlaying) {
            player.pause()
            val progress = getProgress()
            val time = getTimeText()
            _audioState.value = AudioState.Paused(surah, reciter.name, progress, time)
        } else {
            player.start()
            val progress = getProgress()
            val time = getTimeText()
            _audioState.value = AudioState.Playing(surah, reciter.name, progress, time)
            startProgressTracker()
        }
    }

    fun stopPlayback() {
        stopProgressTracker()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _audioState.value = AudioState.Idle
    }

    fun downloadSurah(surahNum: Int, reciter: Reciter) {
        // Simulate real file caching / download offline
        val key = "${surahNum}_${reciter.id}"
        val current = _downloadedSurahs.value.toMutableSet()
        current.add(key)
        _downloadedSurahs.value = current
    }

    fun deleteDownload(surahNum: Int, reciter: Reciter) {
        val key = "${surahNum}_${reciter.id}"
        val current = _downloadedSurahs.value.toMutableSet()
        current.remove(key)
        _downloadedSurahs.value = current
    }

    private fun getProgress(): Float {
        val player = mediaPlayer ?: return 0f
        return player.currentPosition.toFloat() / player.duration.toFloat()
    }

    private fun getTimeText(): String {
        val player = mediaPlayer ?: return "00:00"
        val current = player.currentPosition / 1000
        val duration = player.duration / 1000
        val currentMin = current / 60
        val currentSec = current % 60
        val durMin = duration / 60
        val durSec = duration % 60
        return String.format("%02d:%02d / %02d:%02d", currentMin, currentSec, durMin, durSec)
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        updateThread = Thread {
            try {
                while (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                    val progress = getProgress()
                    val time = getTimeText()
                    _audioState.value = AudioState.Playing(activeSurah, activeReciter.name, progress, time)
                    Thread.sleep(1000)
                }
            } catch (e: InterruptedException) {
                // Thread stopped
            }
        }.apply { start() }
    }

    private fun stopProgressTracker() {
        updateThread?.interrupt()
        updateThread = null
    }
}
