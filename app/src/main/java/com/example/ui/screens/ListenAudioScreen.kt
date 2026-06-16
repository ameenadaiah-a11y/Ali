@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel
import com.example.ui.theme.GoldAccent

@Composable
fun ListenAudioScreen(viewModel: QuranViewModel) {
    val reciters = listOf(
        "الشيخ عبد الباسط عبد الصمد",
        "الشيخ محمود خليل الحصري",
        "الشيخ محمد صديق المنشاوي",
        "الشيخ ماهر المعيقلي",
        "الشيخ أحمد بن علي العجمي",
        "الشيخ سعود الشريم"
    )

    val surahs by viewModel.surahs.collectAsState()
    val activePlayingSurahNum by viewModel.activePlayingSurahNum.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()
    val currentPositionText by viewModel.currentPositionText.collectAsState()
    val durationText by viewModel.durationText.collectAsState()

    val currentReciter by viewModel.selectedReciter.collectAsState()
    val isPlaying by viewModel.isPlayingAudio.collectAsState()
    val currentSpeed by viewModel.playSpeed.collectAsState()
    val sleepTimer by viewModel.sleepTimerMinutes.collectAsState()
    var isDownloadedMap = remember { mutableStateMapOf<String, Boolean>() }

    val currentPlayingSurah = surahs.find { it.number == activePlayingSurahNum }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("المشغل الصوتي وتلاوات القراء", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(QuranScreen.Home) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Reciting Display Core Deck Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("audio_playback_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentReciter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (currentPlayingSurah != null) "تشغيل: ${currentPlayingSurah.arabicName} • جزء ${currentPlayingSurah.juzNumber}" else "تلاوة مرتلة بالروايات المشهورة",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldAccent
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Controls Deck Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(28.dp)
                        ) {
                            IconButton(onClick = { viewModel.playPreviousSurah() }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "سابق", tint = Color.White, modifier = Modifier.size(32.dp))
                            }

                            IconButton(
                                onClick = { viewModel.toggleAudioPlaying() },
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(GoldAccent)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            IconButton(onClick = { viewModel.playNextSurah() }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "لاحق", tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }

                        // Progress slider
                        Spacer(modifier = Modifier.height(20.dp))
                        Slider(
                            value = playbackProgress,
                            onValueChange = {},
                            colors = SliderDefaults.colors(
                                thumbColor = GoldAccent,
                                activeTrackColor = GoldAccent,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(currentPositionText, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(durationText, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Audio Speed and Sleep Timers settings
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "إعدادات التشغيل المتقدمة:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Speed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("سرعة التلاوة: ${currentSpeed}x", fontSize = 13.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PlaybackSpeedBtn("1.0x", currentSpeed == 1.0f) { viewModel.setPlaySpeed(1.0f) }
                                PlaybackSpeedBtn("1.25x", currentSpeed == 1.25f) { viewModel.setPlaySpeed(1.25f) }
                                PlaybackSpeedBtn("1.5x", currentSpeed == 1.5f) { viewModel.setPlaySpeed(1.5f) }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sleep timer count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "مؤقت النوم: ${if (sleepTimer == 0) "متوقف" else "$sleepTimer دقيقة"}",
                                fontSize = 13.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                PlaybackSpeedBtn("إيقاف", sleepTimer == 0) { viewModel.setSleepTimer(0) }
                                PlaybackSpeedBtn("15 د", sleepTimer == 15) { viewModel.setSleepTimer(15) }
                                PlaybackSpeedBtn("30 د", sleepTimer == 30) { viewModel.setSleepTimer(30) }
                            }
                        }
                    }
                }
            }

            // Reciters List
            item {
                Text(
                    text = "اختر القارئ المفضل لديك للفريضة:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(reciters) { name ->
                val isSelected = currentReciter == name
                val isDownloaded = isDownloadedMap[name] ?: false

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectReciter(name) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Headset,
                                contentDescription = null,
                                tint = if (isSelected) GoldAccent else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = name,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Download Offline Mock Manager
                        IconButton(onClick = { isDownloadedMap[name] = !isDownloaded }) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                contentDescription = "تحميل التلاوة أوفلاين",
                                tint = if (isDownloaded) Color.Green else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaybackSpeedBtn(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}
