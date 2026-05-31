package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SurahEntity
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredSurahs by viewModel.filteredSurahs.collectAsState()
    val lastReadSurahNum by viewModel.lastReadSurahNum.collectAsState()
    val lastReadAyahNum by viewModel.lastReadAyahNum.collectAsState()
    val surahsList by viewModel.surahs.collectAsState()
    val configState by viewModel.wirdConfig.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Header Area with Islamic Canvas Crest
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Draw Canvas for Islamic Crest
                    Canvas(
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("islamic_crest_logo")
                    ) {
                        val goldColor = Color(0xFFC5A059)
                        
                        // Outer Golden Circle
                        drawCircle(
                            color = goldColor,
                            radius = size.width / 2f,
                            style = Stroke(width = 4.dp.toPx())
                        )
                        // Inner Decagram / Islamic Pattern Star
                        val numPoints = 8
                        val outerRadius = size.width / 2f - 6.dp.toPx()
                        val innerRadius = outerRadius / 1.5f
                        val path = androidx.compose.ui.graphics.Path()
                        for (i in 0 until (numPoints * 2)) {
                            val angle = i * Math.PI / numPoints
                            val radius = if (i % 2 == 0) outerRadius else innerRadius
                            val x = size.width / 2f + (radius * Math.cos(angle)).toFloat()
                            val y = size.height / 2f + (radius * Math.sin(angle)).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(path = path, color = goldColor, style = Stroke(width = 3.dp.toPx()))

                        // Mini circle inside represent center
                        drawCircle(
                            color = goldColor,
                            radius = 6.dp.toPx()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "القرآن الكريم",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "قرآن يتلى آناء الليل وأطراف النهار",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Last read Quick Access Card (Auto-save)
                    val lastReadSurah = surahsList.find { it.number == lastReadSurahNum }
                    if (lastReadSurah != null) {
                        Card(
                            onClick = { viewModel.selectSurah(lastReadSurah) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("last_read_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Book,
                                        contentDescription = "آخر موضع قراءة",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "متابعة القراءة",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "سورة ${lastReadSurah.arabicName} : الآية $lastReadAyahNum",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                AssistChip(
                                    onClick = { viewModel.selectSurah(lastReadSurah) },
                                    label = { Text("قراءة", color = MaterialTheme.colorScheme.secondary) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    // Wird Progress Overview Card
                    val targetDays = configState?.targetDays ?: 30
                    val currentPage = configState?.currentPage ?: 0
                    val totalPages = 604
                    val progressPercent = ((currentPage.toFloat() / totalPages) * 100).toInt()
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Card(
                        onClick = { viewModel.navigateTo(QuranScreen.WirdScreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_wird_tracker_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "الْوِرْدُ الْيَوْمِيُّ",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "الْوِرْدُ الْيَوْمِيُّ (ختم في $targetDays يوماً)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "أنجزت $progressPercent% من الختمة ($currentPage صفحة من 604)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            AssistChip(
                                onClick = { viewModel.navigateTo(QuranScreen.WirdScreen) },
                                label = { Text("الورد اليومي", color = MaterialTheme.colorScheme.secondary) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // Real-time Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("surah_search_input"),
                placeholder = { Text("ابحث باسم السورة أو بالرقم...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            // Direct Search verses button shortcut
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.navigateTo(QuranScreen.SearchScreen) },
                    modifier = Modifier.testTag("goto_verses_search_btn")
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("البحث بالكلمات في الآيات", fontWeight = FontWeight.Bold)
                }
            }

            // Surahs list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    SurahItemRow(
                        surah = surah,
                        onClick = { viewModel.selectSurah(surah) }
                    )
                }
            }
        }
    }
}

@Composable
fun SurahItemRow(
    surah: SurahEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("surah_${surah.number}_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Surah Number inside design
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.number.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = surah.arabicName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${surah.englishName} • ${surah.totalVerses} آية",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Revelation place badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (surah.revelationPlace == "مكية") 
                            Color(0xFFE8F5E9) 
                        else 
                            Color(0xFFE3F2FD)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = surah.revelationPlace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (surah.revelationPlace == "مكية") Color(0xFF1B5E20) else Color(0xFF0D47A1)
                )
            }
        }
    }
}
