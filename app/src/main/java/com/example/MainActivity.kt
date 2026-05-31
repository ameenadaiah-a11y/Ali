package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: QuranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                val activeScreen by viewModel.activeScreenState.collectAsState()

                // Determine active navigation bottom-tab selection based on active screen
                val selectedTab = when (activeScreen) {
                    is QuranScreen.Home, is QuranScreen.Reading, is QuranScreen.SearchScreen, is QuranScreen.WirdScreen -> 0
                    is QuranScreen.AudioPlayerScreen -> 1
                    is QuranScreen.Adhkar -> 2
                    is QuranScreen.PrayerAndCompass -> 3
                    is QuranScreen.Settings -> 4
                    else -> 0
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier.testTag("app_navigation_bar")
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { viewModel.navigateTo(QuranScreen.Home) },
                                    label = { Text("القرآن") },
                                    icon = { Icon(Icons.Filled.Book, contentDescription = "المصحف") },
                                    modifier = Modifier.testTag("nav_item_quran")
                                )
                                NavigationBarItem(
                                        selected = selectedTab == 1,
                                    onClick = { viewModel.navigateTo(QuranScreen.AudioPlayerScreen) },
                                    label = { Text("التلاوات") },
                                    icon = { Icon(Icons.Filled.VolumeUp, contentDescription = "المشغل الصوتي") },
                                    modifier = Modifier.testTag("nav_item_audio")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { viewModel.navigateTo(QuranScreen.Adhkar) },
                                    label = { Text("الأذكار") },
                                    icon = { Icon(Icons.Filled.Fingerprint, contentDescription = "التسبيح والذكر") },
                                    modifier = Modifier.testTag("nav_item_adhkar")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { viewModel.navigateTo(QuranScreen.PrayerAndCompass) },
                                    label = { Text("القبلة والمواقيت") },
                                    icon = { Icon(Icons.Filled.CompassCalibration, contentDescription = "القبلة ومواقيت الصلاة") },
                                    modifier = Modifier.testTag("nav_item_compass")
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 4,
                                    onClick = { viewModel.navigateTo(QuranScreen.Settings) },
                                    label = { Text("الإعدادات") },
                                    icon = { Icon(Icons.Filled.Settings, contentDescription = "الخيارات") },
                                    modifier = Modifier.testTag("nav_item_settings")
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        val contentModifier = Modifier.padding(innerPadding)
                        
                        when (val screen = activeScreen) {
                            is QuranScreen.Home -> HomeScreen(viewModel, contentModifier)
                            is QuranScreen.Reading -> ReadingScreen(viewModel, screen.surah, contentModifier)
                            is QuranScreen.AudioPlayerScreen -> AudioScreen(viewModel, contentModifier)
                            is QuranScreen.Adhkar -> AdhkarScreen(viewModel, contentModifier)
                            is QuranScreen.PrayerAndCompass -> PrayerCompassScreen(viewModel, contentModifier)
                            is QuranScreen.SearchScreen -> SearchScreen(viewModel, contentModifier)
                            is QuranScreen.Settings -> SettingsScreen(viewModel, contentModifier)
                            is QuranScreen.WirdScreen -> WirdScreen(viewModel, contentModifier)
                            else -> HomeScreen(viewModel, contentModifier)
                        }
                    }
                }
            }
        }
    }
}
