package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AyahEntity
import com.example.data.SurahEntity
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    viewModel: QuranViewModel,
    surah: SurahEntity,
    modifier: Modifier = Modifier
) {
    val ayahs by viewModel.activeAyahs.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedTafsirType by viewModel.selectedTafsirType.collectAsState()
    val selectedReciter by viewModel.selectedPlayReciter.collectAsState()

    val context = LocalContext.current
    var expandedAyahId by remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.navigateTo(QuranScreen.Home) },
                            modifier = Modifier.testTag("back_to_home")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = "سورة ${surah.arabicName}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${surah.revelationPlace} • ${surah.totalVerses} آية",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    actions = {
                        // Quick Audio Play Button
                        IconButton(
                            onClick = {
                                viewModel.audioPlayer.playSurah(surah.number, selectedReciter)
                                viewModel.navigateTo(QuranScreen.AudioPlayerScreen)
                            },
                            modifier = Modifier.testTag("play_surah_audio_btn")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "استماع بصوت الشيخ", tint = MaterialTheme.colorScheme.secondary)
                        }

                        // Font size minus
                        IconButton(
                            onClick = { viewModel.setFontSize(fontSize - 2f) },
                            modifier = Modifier.testTag("decrease_font")
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "تصغير الخط")
                        }
                        
                        // Font size plus
                        IconButton(
                            onClick = { viewModel.setFontSize(fontSize + 2f) },
                            modifier = Modifier.testTag("increase_font")
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "تكبير الخط")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            modifier = modifier
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Bismillah Header (Except for Surah At-Tawbah (9) - standard rule)
                if (surah.number != 9 && surah.number != 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (ayahs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 64.dp)
                    ) {
                        items(ayahs, key = { it.id }) { ayah ->
                            AyahCard(
                                ayah = ayah,
                                fontSize = fontSize,
                                isExpanded = expandedAyahId == ayah.id,
                                selectedTafsirType = selectedTafsirType,
                                onCardClick = {
                                    expandedAyahId = if (expandedAyahId == ayah.id) null else ayah.id
                                    viewModel.saveLastRead(ayah.surahNumber, ayah.ayahNumber)
                                },
                                onBookmarkClick = {
                                    viewModel.toggleBookmarkAyah(ayah)
                                    Toast.makeText(
                                        context,
                                        if (ayah.isBookmarked) "تمت إزالة العلامة المرجعية" else "تمت الإضافة للعلامات المرجعية",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onCopyClick = {
                                    copyToClipboard(context, ayah.textArabic)
                                },
                                onShareClick = {
                                    shareVerse(context, ayah, surah.arabicName)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AyahCard(
    ayah: AyahEntity,
    fontSize: Float,
    isExpanded: Boolean,
    selectedTafsirType: String,
    onCardClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_${ayah.ayahNumber}_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) 
                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardClick() }
                .padding(16.dp)
        ) {
            // Ayah Index Row & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Ayah Number Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.ayahNumber.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Interactive Quick Actions (Bookmark, Copy, Share)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.testTag("bookmark_ayah_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (ayah.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "علامة مرجعية",
                            tint = if (ayah.isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onCopyClick,
                        modifier = Modifier.testTag("copy_ayah_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.testTag("share_ayah_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Beautiful Uthmani Script rendering
            Text(
                text = ayah.textArabic,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                lineHeight = (fontSize * 1.6f).sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            // Dynamic Collapsible Tafsir view below verse
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val tafsirContent = when (selectedTafsirType) {
                    "السعدي" -> ayah.tafsirSaadi
                    "ابن كثير" -> ayah.tafsirKathir
                    "الطبري" -> ayah.tafsirTabari
                    else -> ayah.tafsirSaadi
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تفسير $selectedTafsirType",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tafsirContent,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Justify,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Quran Verse", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "تم نسخ الآية الكريمة للمجافظة", Toast.LENGTH_SHORT).show()
}

private fun shareVerse(context: Context, ayah: AyahEntity, surahName: String) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "قال الله تعالى:\n\n« ${ayah.textArabic} »\n\n[سورة $surahName : الآية ${ayah.ayahNumber}]\n\nتمت المشاركة من تطبيق القرآن الكريم"
        )
    }
    context.startActivity(Intent.createChooser(sendIntent, "شارك الآية الكريمة عبر:"))
}
