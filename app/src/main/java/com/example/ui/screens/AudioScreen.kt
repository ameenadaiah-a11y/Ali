package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioState
import com.example.data.Reciter
import com.example.data.ReciterConfig
import com.example.ui.QuranViewModel

@Composable
fun AudioScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val audioState by viewModel.audioPlayer.audioState.collectAsState()
    val downloadedSurahs by viewModel.audioPlayer.downloadedSurahs.collectAsState()
    val selectedReciter by viewModel.selectedPlayReciter.collectAsState()
    val surahs by viewModel.surahs.collectAsState()

    val context = LocalContext.current

    // Rotation animation logic for playing disk
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue =  0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_angle"
    )

    // Pulse animation logic when music is playing
    val isPlaying = audioState is AudioState.Playing
    val pulseScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pulse_scale"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Title
            Text(
                text = "المشغل الصوتي الشامل",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Dynamic Decorative Rotating Vinyl Disk / Islamic Pattern
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Spinning Star disc
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(if (isPlaying) angle else 0f),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Metadata block
            val surahLabel = when (val state = audioState) {
                is AudioState.Playing -> "سورة ${surahs.find { it.number == state.surahNumber }?.arabicName ?: state.surahNumber}"
                is AudioState.Paused -> "سورة ${surahs.find { it.number == state.surahNumber }?.arabicName ?: state.surahNumber}"
                is AudioState.Loading -> "جاري التحميل والتجهيز..."
                is AudioState.Error -> "فشل التحميل"
                else -> "اختر سورة للبدء بالاستماع"
            }

            val reciterLabel = when (val state = audioState) {
                is AudioState.Playing -> "بصوت القارئ: ${state.reciterName}"
                is AudioState.Paused -> "بصوت القارئ: ${state.reciterName}"
                else -> "القارئ الحالي: ${selectedReciter.name}"
            }

            Text(
                text = surahLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reciterLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time & Slider Progress Trackers
            val currentProgress = when (val state = audioState) {
                is AudioState.Playing -> state.progress
                is AudioState.Paused -> state.progress
                else -> 0f
            }

            val timeString = when (val state = audioState) {
                is AudioState.Playing -> state.timeText
                is AudioState.Paused -> state.timeText
                else -> "00:00 / 00:00"
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = timeString, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (audioState is AudioState.Loading) {
                        Text(text = "متصل بالخادم...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            // Real controller buttons row (Play, Pause, stop, simulated download triggers)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simulated Offline Download button in player
                val isActiveDownloaded = when (val state = audioState) {
                    is AudioState.Playing -> downloadedSurahs.contains("${state.surahNumber}_${selectedReciter.id}")
                    is AudioState.Paused -> downloadedSurahs.contains("${state.surahNumber}_${selectedReciter.id}")
                    else -> false
                }
                
                IconButton(
                    onClick = {
                        val activeNum = when (val state = audioState) {
                            is AudioState.Playing -> state.surahNumber
                            is AudioState.Paused -> state.surahNumber
                            else -> null
                        }
                        if (activeNum != null) {
                            if (isActiveDownloaded) {
                                viewModel.audioPlayer.deleteDownload(activeNum, selectedReciter)
                                Toast.makeText(context, "تم حذف السورة من المذكرة المحلية", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.audioPlayer.downloadSurah(activeNum, selectedReciter)
                                Toast.makeText(context, "تم تحميل السورة بنجاح للاستماع دون إنترنت✅", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "يرجى تشغيل سورة أولاً لتحميلها", Toast.LENGTH_SHORT).show()
                
                    }
                    },
                    modifier = Modifier.testTag("download_surah_btn")
                ) {
                    Icon(
                        imageVector = if (isActiveDownloaded) Icons.Filled.DownloadDone else Icons.Filled.Download,
                        contentDescription = "تحميل للاستماع بدون نت",
                        tint = if (isActiveDownloaded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Play / Pause core toggle button
                IconButton(
                    onClick = { viewModel.audioPlayer.togglePlayPause() },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("play_pause_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "تشغيل وقوف مؤقت",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Stop playback button
                IconButton(
                    onClick = { viewModel.audioPlayer.stopPlayback() },
                    modifier = Modifier.testTag("stop_play_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "إيقاف",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Sheikh / Reciter Quick Changer Row (Horizontal)
            Text(
                text = "اختر القارئ المفضل:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReciterConfig.list.forEach { reciter ->
                    val isSelected = selectedReciter.id == reciter.id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                viewModel.selectReciter(reciter)
                                Toast.makeText(context, "القارئ الحالي: ${reciter.name}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("reciter_${reciter.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reciter.name.split(" ")[0] + "\n" + (reciter.name.split(" ").getOrNull(1) ?: ""),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // All Surahs click-to-play catalog in player screem
            Text(
                text = "قائمة السور السريعة:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 64.dp)
            ) {
                items(surahs) { surah ->
                    val isCurrentlySelected = when (val state = audioState) {
                        is AudioState.Playing -> state.surahNumber == surah.number
                        is AudioState.Paused -> state.surahNumber == surah.number
                        else -> false
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCurrentlySelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable {
                                viewModel.audioPlayer.playSurah(surah.number, selectedReciter)
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCurrentlySelected) Icons.Filled.VolumeUp else Icons.Filled.PlayCircleFilled,
                                contentDescription = null,
                                tint = if (isCurrentlySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "سورة ${surah.arabicName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCurrentlySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        // Download badge offline check
                        val isDownloaded = downloadedSurahs.contains("${surah.number}_${selectedReciter.id}")
                        if (isDownloaded) {
                            Icon(
                                imageVector = Icons.Filled.WifiOff,
                                contentDescription = "جاهز دون اتصال بنرنت",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
