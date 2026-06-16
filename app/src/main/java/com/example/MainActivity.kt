package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.data.QuranDataHelper
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel
import com.example.ui.screens.*
import com.example.ui.theme.QuranKareemTheme

class MainActivity : ComponentActivity() {

    private val viewModel: QuranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable premium Edge-to-Edge safe drawing
        enableEdgeToEdge()

        // Seed initial databases (Surahs, Ayahs, Adhkar) on initial run
        val db = com.example.data.QuranDatabase.getDatabase(this)
        val repository = com.example.data.QuranRepository(db.quranDao())
        QuranDataHelper.populateDatabase(this, lifecycleScope, repository)

        setContent {
            val currentScreen by viewModel.currentScreen.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (viewModel.isDarkThemeValue.value) {
                null -> systemDark
                true -> true
                false -> false
            }

            QuranKareemTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_root_surface")
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val screen = currentScreen) {
                        is QuranScreen.Home -> HomeScreen(viewModel)
                        is QuranScreen.Reading -> ReadingScreen(viewModel, screen.surahNumber)
                        is QuranScreen.Adhkar -> AdhkarScreen(viewModel)
                        is QuranScreen.Notes -> NotesScreen(viewModel)
                        is QuranScreen.PrayerTimes -> PrayerTimesScreen(viewModel)
                        is QuranScreen.Qibla -> QiblaScreen(viewModel)
                        is QuranScreen.HifzPlans -> HifzPlansScreen(viewModel)
                        is QuranScreen.StatisticalDashboard -> StatisticalDashboardScreen(viewModel)
                        is QuranScreen.GeminiAssistant -> GeminiAssistantScreen(viewModel)
                        is QuranScreen.ListenAudio -> ListenAudioScreen(viewModel)
                        is QuranScreen.Settings -> SettingsScreen(viewModel)
                    }
                }
            }
        }
    }
}
