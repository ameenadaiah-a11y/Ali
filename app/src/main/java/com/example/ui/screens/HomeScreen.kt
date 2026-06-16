package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SurahEntity
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel
import com.example.ui.theme.GoldAccent

@Composable
fun HomeScreen(viewModel: QuranViewModel) {
    val surahs by viewModel.surahs.collectAsState()
    val readingHistory by viewModel.readingHistory.collectAsState()
    val downloadedSurahsCount by viewModel.downloadedSurahsCount.collectAsState()
    val isDownloadingRealQuran by viewModel.isDownloadingRealQuran.collectAsState()
    val downloadProgressString by viewModel.downloadProgressString.collectAsState()
    
    // States for search and organization tabs
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = الفهرس الشامل, 1 = الأجزاء الثلاثون, 2 = مكان النزول, 3 = حسب طول السور
    var sortAscending by remember { mutableStateOf(false) } // For Tab 3 lengths: false = longest first, true = shortest first
    var expandedJuz by remember { mutableStateOf<Int?>(null) } // For expanding individual parts
    var revelationFilter by remember { mutableStateOf("الكل") } // For Tab 2 filter: "الكل", "مكية", "مدنية"

    val filteredSurahs = remember(surahs, searchQuery) {
        surahs.filter {
            it.arabicName.contains(searchQuery) || it.englishName.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // Precalculate Juz groupings at Composable top level
    val juzInfoList = remember(surahs) {
        (1..30).map { jNum ->
            val startingSurahs = surahs.filter { it.juzNumber == jNum }
            val continuingSurah = if (startingSurahs.isEmpty()) {
                surahs.lastOrNull { it.juzNumber < jNum }
            } else {
                null
            }
            Triple(jNum, startingSurahs, continuingSurah)
        }
    }

    // Precalculate Sorted Lists at Composable top level
    val sortedByLength = remember(filteredSurahs, sortAscending) {
        if (sortAscending) {
            filteredSurahs.sortedBy { it.numberOfAyahs }
        } else {
            filteredSurahs.sortedByDescending { it.numberOfAyahs }
        }
    }

    // Precalculate Revelation Filter lists at Composable top level
    val revelationFiltered = remember(filteredSurahs, revelationFilter) {
        filteredSurahs.filter {
            revelationFilter == "الكل" || it.revelationType == revelationFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_column")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Majestic Header Card with Gradient
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "مصحف شهاب عداية",
                                style = MaterialTheme.typography.headlineLarge,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "بوابة الوحي الإلهي - صدقة جارية وتطبيق مطور للمهندس شهاب عداية",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )
                    }
                    
                    // Stats quick row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📖 ${surahs.size} سورة موثقة",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "🕌 ٣٠ جزء كامل",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Premium Offline Download Progress Dashboard Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_download_dashboard_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (downloadedSurahsCount == 114) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = if (downloadedSurahsCount == 114) Color(0xFF2E7D32) else GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = if (downloadedSurahsCount == 114) "المصحف كامل 100% أوفلاين" else "تحميل المصحف كاملًا أوفلاين",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "تم تنزيل وتثبيت $downloadedSurahsCount من أصل 114 سورة بشكل صحيح",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        if (downloadedSurahsCount < 114) {
                            if (isDownloadingRealQuran) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = GoldAccent,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Button(
                                    onClick = { viewModel.downloadAllRealSurahs() },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("تنزيل الكل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    if (isDownloadingRealQuran) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = downloadedSurahsCount.toFloat() / 114f,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = GoldAccent,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = downloadProgressString,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldAccent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (downloadedSurahsCount < 114) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = downloadedSurahsCount.toFloat() / 114f,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = GoldAccent,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "تلاوات وبيانات سور المصحف الشريف بالكامل تعمل أوفلاين 100% وبسرعة فائقة.",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Last Read Position Resume Trigger
        item {
            AnimatedVisibility(visible = readingHistory != null) {
                readingHistory?.let { history ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.navigateTo(QuranScreen.Reading(history.surahNumber))
                            }
                            .testTag("resume_reading_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
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
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "متابعة القراءة",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "سورة ${history.surahName} - الآية ${history.ayahNumber} (صفحة ${history.pageNumber})",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Quick Grid Action Blocks
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الخدمات السريعة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionTile("الأذكار", Icons.Default.Favorite, Modifier.weight(1f)) {
                        viewModel.navigateTo(QuranScreen.Adhkar)
                    }
                    QuickActionTile("مواقيت الصلاة", Icons.Default.Schedule, Modifier.weight(1f)) {
                        viewModel.navigateTo(QuranScreen.PrayerTimes)
                    }
                    QuickActionTile("القبلة", Icons.Default.Explore, Modifier.weight(1f)) {
                        viewModel.navigateTo(QuranScreen.Qibla)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionTile("ملاحظاتي", Icons.Default.EditNote, Modifier.weight(1f)) {
                        viewModel.navigateTo(QuranScreen.Notes)
                    }
                    QuickActionTile("الحفظ والمراجعة", Icons.Default.School, Modifier.weight(1f)) {
                        viewModel.navigateTo(QuranScreen.HifzPlans)
                    }
                    QuickActionTile("الإحصائيات", Icons.Default.BarChart, Modifier.weight(1f)) {
                        viewModel.navigateTo(QuranScreen.StatisticalDashboard)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionTile("المساعد الذكي AI", Icons.Default.AutoAwesome, Modifier.weight(1.5f)) {
                        viewModel.navigateTo(QuranScreen.GeminiAssistant)
                    }
                    QuickActionTile("الاستماع الصوتي", Icons.Default.Headset, Modifier.weight(1.5f)) {
                        viewModel.navigateTo(QuranScreen.ListenAudio)
                    }
                }
            }
        }

        // Section Title: تنظيم وتحليل الفهرس الشامل
        item {
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "بوابة التنظيم القرآني",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }
        }

        // Custom Scrollable Row of Pill Switches (Categorization Tabs)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple(0, "الفهرس العام", Icons.Default.FormatListNumbered),
                    Triple(1, "الأجزاء الـ ٣٠", Icons.Default.GridView),
                    Triple(2, "مكان النزول", Icons.Default.FilterVintage),
                    Triple(3, "طول السور", Icons.Default.SortByAlpha)
                ).forEach { (index, title, icon) ->
                    OrganizedTabPill(
                        label = title,
                        icon = icon,
                        isSelected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }

        // Render Search Bar Only on Tabs that benefit from direct search filtering and listing (Tab 0, 2, 3)
        if (selectedTab == 0 || selectedTab == 2 || selectedTab == 3) {
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("بحث عن سورة بصفات واسم محدد...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }
            }
        }

        // --- DYNAMIC SECTIONS BASED ON SELECT_TAB ---
        when (selectedTab) {
            0 -> {
                // TAB 0: Complete General Index List
                if (filteredSurahs.isEmpty()) {
                    item {
                        EmptyStateView(text = "لا توجد نتائج مطابقة لبحثك")
                    }
                } else {
                    items(filteredSurahs) { surah ->
                        SurahRow(surah) {
                            viewModel.navigateTo(QuranScreen.Reading(surah.number))
                        }
                    }
                }
            }
            1 -> {
                // TAB 1: 30 Parts list layout (Expandable sections)
                items(juzInfoList) { (jNum, startingSurahs, continuingSurah) ->
                    val isExpanded = expandedJuz == jNum
                    JuzCard(
                        juzNumber = jNum,
                        startingSurahs = startingSurahs,
                        continuingSurah = continuingSurah,
                        isExpanded = isExpanded,
                        onExpandClick = {
                            expandedJuz = if (isExpanded) null else jNum
                        },
                        onSurahClick = { surahNumber ->
                            viewModel.navigateTo(QuranScreen.Reading(surahNumber))
                        }
                    )
                }
            }
            2 -> {
                // TAB 2: Revelation Sort (Meccan vs Medinan with Summary Analytics Cards)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { revelationFilter = "مكية" },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                border = if (revelationFilter == "مكية") BorderStroke(1.5.dp, Color(0xFF2E7D32)) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Icon(
                                        Icons.Default.FilterVintage,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "٨٦ سورة مكية",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "نزلت بمكة وتتميز بآيات العقيدة والتوحيد الطاهرة",
                                        fontSize = 10.sp,
                                        color = Color(0xFF2E7D32),
                                        lineHeight = 14.sp
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { revelationFilter = "مدنية" },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                border = if (revelationFilter == "مدنية") BorderStroke(1.5.dp, Color(0xFFE65100)) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Icon(
                                        Icons.Default.LocationCity,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "٢٨ سورة مدنية",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFFE65100)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "نزلت بالمدينة مع نصوص التشريعات والأحكام والعهود",
                                        fontSize = 10.sp,
                                        color = Color(0xFFEF6C00),
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }

                        // Sorting Filter Sub-tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("فلترة سريعة:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            
                            listOf("الكل", "مكية", "مدنية").forEach { mode ->
                                val active = revelationFilter == mode
                                SortPill(
                                    label = mode,
                                    icon = if (mode == "مكية") Icons.Default.FilterVintage else if (mode == "مدنية") Icons.Default.LocationCity else Icons.Default.FormatListNumbered,
                                    isSelected = active,
                                    onClick = { revelationFilter = mode }
                                )
                            }
                        }
                    }
                }

                if (revelationFiltered.isEmpty()) {
                    item {
                        EmptyStateView(text = "لا توجد سور مطابقة للفلاتر المحددة")
                    }
                } else {
                    items(revelationFiltered) { surah ->
                        SurahRow(surah) {
                            viewModel.navigateTo(QuranScreen.Reading(surah.number))
                        }
                    }
                }
            }
            3 -> {
                // TAB 3: Sort by length / verses count
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "فرز السور حسب الطول ومعدل الآيات:",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SortPill(
                                label = "الأطول أولاً",
                                icon = Icons.Default.ArrowDownward,
                                isSelected = !sortAscending,
                                onClick = { sortAscending = false }
                            )
                            SortPill(
                                label = "الأقصر أولاً",
                                icon = Icons.Default.ArrowUpward,
                                isSelected = sortAscending,
                                onClick = { sortAscending = true }
                            )
                        }
                    }
                }

                if (sortedByLength.isEmpty()) {
                    item {
                        EmptyStateView(text = "لا توجد سور مطابقة")
                    }
                } else {
                    items(sortedByLength) { surah ->
                        SurahRow(surah) {
                            viewModel.navigateTo(QuranScreen.Reading(surah.number))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = text,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun QuickActionTile(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(75.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OrganizedTabPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) GoldAccent.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .border(
                1.dp,
                if (isSelected) GoldAccent else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun SortPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun RubElHizbIcon(number: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(45.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val side = width
            
            // Draw two overlapping squares rotated by 45 degrees
            drawContext.canvas.save()
            drawContext.canvas.translate(width / 2f, height / 2f)
            
            val rectSize = side * 0.70f
            
            // First square
            drawRect(
                color = GoldAccent.copy(alpha = 0.12f),
                topLeft = androidx.compose.ui.geometry.Offset(-rectSize / 2f, -rectSize / 2f),
                size = androidx.compose.ui.geometry.Size(rectSize, rectSize)
            )
            
            // Golden outline of first square
            drawRect(
                color = GoldAccent,
                topLeft = androidx.compose.ui.geometry.Offset(-rectSize / 2f, -rectSize / 2f),
                size = androidx.compose.ui.geometry.Size(rectSize, rectSize),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
            )
            
            // Rotate 45 degrees
            drawContext.canvas.rotate(45f)
            
            // Second square background
            drawRect(
                color = GoldAccent.copy(alpha = 0.12f),
                topLeft = androidx.compose.ui.geometry.Offset(-rectSize / 2f, -rectSize / 2f),
                size = androidx.compose.ui.geometry.Size(rectSize, rectSize)
            )
            
            // Second square outline
            drawRect(
                color = GoldAccent,
                topLeft = androidx.compose.ui.geometry.Offset(-rectSize / 2f, -rectSize / 2f),
                size = androidx.compose.ui.geometry.Size(rectSize, rectSize),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
            )
            
            drawContext.canvas.restore()
            
            // Outer circular golden border with small radius
            drawCircle(
                color = GoldAccent.copy(alpha = 0.08f),
                radius = width * 0.22f
            )
        }
        
        Text(
            text = number.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SurahRow(surah: SurahEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("surah_row_${surah.number}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Right-side (Islamic star number + names)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                RubElHizbIcon(number = surah.number)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = surah.arabicName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Small tag for revelation type (Meccan / Medinan)
                        val isMeccan = surah.revelationType == "مكية"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isMeccan) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = surah.revelationType,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMeccan) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${surah.englishName} • ${surah.name}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Left-side (Verse count and start page/Juz info)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${surah.numberOfAyahs} آية",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "جزء ${surah.juzNumber}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "صفحة ${surah.startPage}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun JuzCard(
    juzNumber: Int,
    startingSurahs: List<SurahEntity>,
    continuingSurah: SurahEntity?,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    onSurahClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isExpanded) GoldAccent.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = juzNumber.toString(),
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "الجزء $juzNumber",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (startingSurahs.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "تبدأ فيه ${startingSurahs.size} سور",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (continuingSurah != null) {
                        Text(
                            text = "مستمر في سورة ${continuingSurah.arabicName}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                
                // Show Surahs inside this Juz
                if (startingSurahs.isNotEmpty()) {
                    Text(
                        text = "السور المبتدئة في هذا الجزء:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    startingSurahs.forEach { surah ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSurahClick(surah.number) }
                                .padding(vertical = 8.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "سورة ${surah.arabicName}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${surah.englishName})",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${surah.numberOfAyahs} آية",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "بداية صفحة ${surah.startPage}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else if (continuingSurah != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSurahClick(continuingSurah.number) }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "لا تبدأ سور جديدة في هذا الجزء. هو استكمال لآيات سورة ${continuingSurah.arabicName}.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "فتح سورة ${continuingSurah.arabicName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
