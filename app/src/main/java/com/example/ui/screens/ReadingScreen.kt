@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AyahEntity
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ReadingScreen(viewModel: QuranViewModel, surahNumber: Int) {
    val surahs by viewModel.surahs.collectAsState()
    val ayahs by viewModel.currentAyahs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val tafsirMode by viewModel.selectedTafsirMode.collectAsState()
    val isUpdatingSurah by viewModel.isUpdatingSurah.collectAsState()
    
    val currentSurah = surahs.find { it.number == surahNumber }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Preferences and Modes
    var isMushafMode by remember { mutableStateOf(true) }
    val fontSizeMultiplier by remember { viewModel.fontSizeMultiplier }
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (viewModel.isDarkThemeValue.value) {
        null -> isSystemDark
        true -> true
        false -> false
    }

    // Traditional Page Colors
    val paperBgColor = if (isDark) Color(0xFF1C1A17) else Color(0xFFFAF6EE)
    val paperTextColor = if (isDark) Color(0xFFEADCC9) else Color(0xFF2C251F)
    val paperBorderColor = if (isDark) Color(0xFF7A6441) else Color(0xFFDCC8A7)

    // Layout pagination calculations
    val groupedPages = remember(ayahs) { ayahs.groupBy { it.page }.toSortedMap() }
    val pageList = remember(groupedPages) { groupedPages.keys.toList() }
    var activePageIdx by remember { mutableStateOf(0) }

    // Dialog & bottom panel controllers
    var selectedAyahForAction by remember { mutableStateOf<AyahEntity?>(null) }
    var noteInputForSelectedAyah by remember { mutableStateOf("") }
    var showJuzDialog by remember { mutableStateOf(false) }
    var showPageDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showOurWorksDialog by remember { mutableStateOf(false) }
    var showAudioPlayerSheet by remember { mutableStateOf(false) }

    // Sync active page index with history/bookmarks
    LaunchedEffect(surahNumber) {
        viewModel.loadAyahsForSurah(surahNumber)
    }

    LaunchedEffect(pageList) {
        if (pageList.isNotEmpty()) {
            val history = viewModel.readingHistory.value
            if (history != null && history.surahNumber == surahNumber) {
                val idx = pageList.indexOf(history.pageNumber)
                if (idx >= 0) {
                    activePageIdx = idx
                }
            }
        }
    }

    val activePageNumber = remember(pageList, activePageIdx) {
        if (pageList.isNotEmpty()) pageList[activePageIdx] else 1
    }

    val ayahsOfActivePage = remember(groupedPages, activePageNumber) {
        groupedPages[activePageNumber] ?: emptyList()
    }

    // Save Reading History on Page Change
    LaunchedEffect(activePageNumber, ayahsOfActivePage) {
        if (ayahsOfActivePage.isNotEmpty() && currentSurah != null) {
            val firstAyah = ayahsOfActivePage.first()
            viewModel.saveLastReadPosition(
                surahNum = surahNumber,
                surahName = currentSurah.arabicName,
                ayahNum = firstAyah.ayahNumber,
                pageNum = activePageNumber
            )
        }
    }

    // Helper translation string
    fun getJuzArabicName(juzNum: Int): String {
        return when(juzNum) {
            1 -> "الجزء الأول"
            2 -> "الجزء الثاني"
            3 -> "الجزء الثالث"
            4 -> "الجزء الرابع"
            5 -> "الجزء الخامس"
            6 -> "الجزء السادس"
            7 -> "الجزء السابع"
            8 -> "الجزء الثامن"
            9 -> "الجزء التاسع"
            10 -> "الجزء العاشر"
            11 -> "الجزء الحادي عشر"
            12 -> "الجزء الثاني عشر"
            13 -> "الجزء الثالث عشر"
            14 -> "الجزء الرابع عشر"
            15 -> "الجزء الخامس عشر"
            16 -> "الجزء السادس عشر"
            17 -> "الجزء السابع عشر"
            18 -> "الجزء الثامن عشر"
            19 -> "الجزء التاسع عشر"
            20 -> "الجزء العشرون"
            21 -> "الجزء الحادي والعشرون"
            22 -> "الجزء الثاني والعشرون"
            23 -> "الجزء الثالث والعشرون"
            24 -> "الجزء الرابع والعشرون"
            25 -> "الجزء الخامس والعشرون"
            26 -> "الجزء السادس والعشرون"
            27 -> "الجزء السابع والعشرون"
            28 -> "الجزء الثامن والعشرون"
            29 -> "الجزء التاسع والعشرون"
            30 -> "الجزء الثلاثون"
            else -> "الجزء $juzNum"
        }
    }

    Scaffold(
        topBar = {
            if (!isMushafMode) {
                SmallTopAppBar(
                    title = {
                        Text(
                            text = currentSurah?.arabicName ?: "تلاوة القرآن",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateTo(QuranScreen.Home) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            isMushafMode = true
                        }) {
                            Icon(Icons.Default.Book, contentDescription = "عرض المصحف")
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isMushafMode) PaddingValues(0.dp) else innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isMushafMode) {
                // --- PREMIUM MUS'HAF GRAPHIC CONTAINER (Traditional Printed Look) ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(paperBgColor)
                ) {
                    // Top Custom Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .statusBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Surah Name
                        Text(
                            text = currentSurah?.arabicName ?: "سورة القرآن",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = paperTextColor,
                            fontFamily = FontFamily.Serif
                        )

                        // Center: Page Number Ornaments
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(paperBorderColor.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Text(
                                text = activePageNumber.toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }

                        // Right: Portion/Juz Label
                        val currentJuzNum = ayahsOfActivePage.firstOrNull()?.juz ?: 1
                        Text(
                            text = getJuzArabicName(currentJuzNum),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = paperTextColor,
                            fontFamily = FontFamily.Serif
                        )
                    }

                    // Divider gold lines
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, paperBorderColor, Color.Transparent)
                                )
                            )
                    )

                    // Page content with traditional borders
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(
                                width = 1.5.dp,
                                color = paperBorderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(3.dp)
                            .border(
                                width = 0.5.dp,
                                color = paperBorderColor.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(14.dp)
                    ) {
                        // Decorative Hanging Ribbon Bookmark (Top-Left corner)
                        val isPageBookmarked = bookmarks.any { it.isPageBookmark && it.pageNumber == activePageNumber }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 12.dp)
                                .size(width = 24.dp, height = 50.dp)
                                .clickable {
                                    viewModel.togglePageBookmark(activePageNumber, surahNumber)
                                    Toast.makeText(
                                        context,
                                        if (isPageBookmarked) "تم إزالة علامة الصفحة" else "تم حفظ علامة الصفحة",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    lineTo(size.width, size.height)
                                    lineTo(size.width / 2f, size.height - 12.dp.toPx())
                                    lineTo(0f, size.height)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = if (isPageBookmarked) Color(0xFFC62828) else paperBorderColor.copy(alpha = 0.4f)
                                )
                            }
                        }
                        var swipeOffsetX by remember { mutableStateOf(0f) }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(activePageIdx, pageList.size) {
                                    detectDragGestures(
                                        onDragStart = { swipeOffsetX = 0f },
                                        onDragEnd = {
                                            if (swipeOffsetX > 150f) {
                                                // Swipe Right -> Prev Page (RTL flow)
                                                if (activePageIdx > 0) {
                                                    activePageIdx--
                                                    selectedAyahForAction = null
                                                }
                                            } else if (swipeOffsetX < -150f) {
                                                // Swipe Left -> Next Page (RTL flow)
                                                if (activePageIdx < pageList.size - 1) {
                                                    activePageIdx++
                                                    selectedAyahForAction = null
                                                }
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            swipeOffsetX += dragAmount.x
                                        }
                                    )
                                }
                        ) {
                            val pageScrollState = rememberScrollState()

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(pageScrollState),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (isUpdatingSurah) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, GoldAccent),
                                        colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.08f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "جاري جلب الآيات الكريمة الموثقة والتشكيل الحقيقي...",
                                                fontSize = 11.sp,
                                                color = GoldAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (ayahsOfActivePage.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = GoldAccent)
                                    }
                                } else {
                                    // Check if this page contains Ayah 1 of any Surah
                                    val hasSurahStart = ayahsOfActivePage.any { it.ayahNumber == 1 }
                                    val startAyahEntity = ayahsOfActivePage.find { it.ayahNumber == 1 }
                                    val startSurahMeta = if (hasSurahStart && startAyahEntity != null) {
                                        surahs.find { it.number == startAyahEntity.surahNumber }
                                    } else null

                                    if (startSurahMeta != null) {
                                        // Decorated Surah Heading Banner
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.5.dp, GoldAccent),
                                            colors = CardDefaults.cardColors(
                                                containerColor = GoldAccent.copy(alpha = 0.08f)
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 10.dp, horizontal = 16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = "سُورَةُ ${startSurahMeta.arabicName}",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldAccent,
                                                    fontFamily = FontFamily.Serif,
                                                    textAlign = TextAlign.Center
                                                )
                                                Text(
                                                    text = "${if (startSurahMeta.revelationType == "Meccan") "مكيّة" else "مدنيّة"} • ${startSurahMeta.numberOfAyahs} آيات",
                                                    fontSize = 12.sp,
                                                    color = paperTextColor.copy(alpha = 0.6f),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        // Render Basmala except for Surah Al-Tawbah (9)
                                        if (startSurahMeta.number != 9) {
                                            Text(
                                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                                fontSize = (24 * fontSizeMultiplier).sp,
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldAccent,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 10.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Build Continuous Unified Quran Scripture page text flow
                                    val annotatedText = buildAnnotatedString {
                                        ayahsOfActivePage.forEach { ayah ->
                                            val startIdx = length
                                            append(ayah.textArabic)
                                            val endIdx = length

                                            addStringAnnotation(
                                                tag = "AYAH_CLICK_TAG",
                                                annotation = ayah.ayahNumber.toString(),
                                                start = startIdx,
                                                end = endIdx
                                            )

                                            // Highlight Ayah on select
                                            if (selectedAyahForAction?.id == ayah.id) {
                                                addStyle(
                                                    style = SpanStyle(
                                                        background = GoldAccent.copy(alpha = 0.28f),
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    start = startIdx,
                                                    end = endIdx
                                                )
                                            }

                                            append(" ")
                                            withStyle(SpanStyle(color = GoldAccent, fontWeight = FontWeight.Bold)) {
                                                append("﴿${ayah.ayahNumber}﴾")
                                            }
                                            append(" ")
                                        }
                                    }

                                    ClickableText(
                                        text = annotatedText,
                                        style = TextStyle(
                                            fontSize = (22 * fontSizeMultiplier).sp,
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.SemiBold,
                                            color = paperTextColor,
                                            textAlign = TextAlign.Right,
                                            lineHeight = (38 * fontSizeMultiplier).sp,
                                            textDirection = TextDirection.Rtl
                                        ),
                                        onClick = { offset ->
                                            annotatedText.getStringAnnotations("AYAH_CLICK_TAG", offset, offset)
                                                .firstOrNull()?.let { annotation ->
                                                    val ayahNum = annotation.item.toIntOrNull()
                                                    if (ayahNum != null) {
                                                        val tappedAyah = ayahsOfActivePage.find { it.ayahNumber == ayahNum }
                                                        if (tappedAyah != null) {
                                                            selectedAyahForAction = tappedAyah
                                                            noteInputForSelectedAyah = ""
                                                        }
                                                    }
                                                }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // --- UPPER DYNAMIC QUICK ACTION TOOLBAR (Overlay strip above Navbar) ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(paperBorderColor.copy(alpha = 0.12f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Theme switcher toggle
                        IconButton(onClick = {
                            viewModel.isDarkThemeValue.value = !isDark
                        }) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.BrightnessHigh else Icons.Default.BrightnessMedium,
                                contentDescription = "تغيير المظهر",
                                tint = GoldAccent
                            )
                        }

                        // Middle controls: Page bookmark toggle
                        val isPageBookmarked = bookmarks.any { it.isPageBookmark && it.pageNumber == activePageNumber }
                        TextButton(
                            onClick = {
                                viewModel.togglePageBookmark(activePageNumber, surahNumber)
                                Toast.makeText(
                                    context,
                                    if (isPageBookmarked) "تم إزالة علامة الصفحة" else "تم حفظ علامة الصفحة",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = if (isPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = GoldAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حفظ علامة",
                                color = paperTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Go to Page Bookmark position
                        val hasBookmarkedPage = bookmarks.any { it.isPageBookmark }
                        TextButton(
                            onClick = {
                                val bmark = bookmarks.firstOrNull { it.isPageBookmark }
                                if (bmark != null) {
                                    if (bmark.surahNumber != surahNumber) {
                                        viewModel.navigateTo(QuranScreen.Reading(bmark.surahNumber))
                                    } else {
                                        val targetIdx = pageList.indexOf(bmark.pageNumber)
                                        if (targetIdx >= 0) {
                                            activePageIdx = targetIdx
                                        }
                                    }
                                    Toast.makeText(context, "تم الانتقال للعلامة المحفوظة", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "لا توجد صفحات محفوظة بعد", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Launch, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "انتقال للعلامة",
                                color = paperTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Audio Recitation Player Button
                        IconButton(onClick = { showAudioPlayerSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.Headset,
                                contentDescription = "تشغيل التلاوة الصوتية",
                                tint = GoldAccent
                            )
                        }

                        // Search Verse Icon
                        IconButton(onClick = { showSearchDialog = true }) {
                            Icon(Icons.Default.Search, contentDescription = "بحث فوري", tint = GoldAccent)
                        }
                    }

                    // --- LOWER PRIMARY NAVIGATION NAVBAR (Traditional Premium Dark Theme Layout - 2 Rows) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E1C1A)) // Dark Premium background
                            .navigationBarsPadding()
                            .padding(vertical = 8.dp)
                    ) {
                        // Row 1 of dark area (Index, Parts, Pages)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. الفهرس Index Button
                            NavMenuItem(
                                label = "الفهرس",
                                icon = Icons.Default.List,
                                onClick = { viewModel.navigateTo(QuranScreen.Home) }
                            )

                            // 2. الأجزاء Juzs Button
                            NavMenuItem(
                                label = "الأجزاء",
                                icon = Icons.Default.GridGoldenratio,
                                onClick = { showJuzDialog = true }
                            )

                            // 3. الصفحات pages selection
                            NavMenuItem(
                                label = "الصفحات",
                                icon = Icons.Default.Layers,
                                onClick = { showPageDialog = true }
                            )
                        }

                        Divider(
                            color = Color(0xFF332F2A),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
                        )

                        // Row 2 of dark area (Share, Works, More)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 4. مشاركة Share
                            NavMenuItem(
                                label = "مشاركة",
                                icon = Icons.Default.Share,
                                onClick = {
                                    val shareTxt = "أقرأ حالياً سورة ${currentSurah?.arabicName ?: ""} صفحة $activePageNumber من القرآن الكريم."
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareTxt)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "مشاركة الصفحة"))
                                }
                            )

                            // 5. أعمالنا works / feature portal
                            NavMenuItem(
                                label = "أعمالنا",
                                icon = Icons.Default.Apps,
                                onClick = { showOurWorksDialog = true }
                            )

                            // 6. المزيد More/View modes
                            NavMenuItem(
                                label = "المزيد",
                                icon = Icons.Default.MoreHoriz,
                                onClick = { showMoreMenu = true }
                            )
                        }
                    }
                }
            } else {
                // --- TRADITIONAL CONTINUOUS TRANSLATION MODE (LIST VIEW) ---
                Column(modifier = Modifier.fillMaxSize()) {
                    // Quick bar to go back to Mushaf mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "عرض متتالي (عربي/إنجليزي/تفسير)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { showAudioPlayerSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.Headset,
                                    contentDescription = "تشغيل التلاوة الصوتية",
                                    tint = GoldAccent
                                )
                            }
                            Button(
                                onClick = { isMushafMode = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("عرض المصحف الكلاسيكي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // TAFSIR QUICK MODE SELECT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "التفسير الحالي: ${if(tafsirMode == "saadi") "السعدي" else if(tafsirMode == "kathir") "ابن كثير" else "الميسر"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TafsirButton("السعدي", "saadi", tafsirMode) { viewModel.selectTafsirMode("saadi") }
                            TafsirButton("ابن كثير", "kathir", tafsirMode) { viewModel.selectTafsirMode("kathir") }
                            TafsirButton("الميسر", "muyassar", tafsirMode) { viewModel.selectTafsirMode("muyassar") }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (surahNumber != 1 && surahNumber != 9) {
                            item {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = (26 * fontSizeMultiplier).sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                )
                            }
                        }

                        itemsIndexed(ayahs) { index, ayah ->
                            val isBookmarked = bookmarks.any { !it.isPageBookmark && it.id == "${ayah.surahNumber}_${ayah.ayahNumber}" }
                            val isSelected = selectedAyahForAction?.id == ayah.id

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        selectedAyahForAction = if (isSelected) null else ayah
                                        noteInputForSelectedAyah = ""
                                    }
                                    .padding(12.dp)
                            ) {
                                // Arabic
                                Text(
                                    text = "${ayah.textArabic} ﴿${ayah.ayahNumber}﴾",
                                    fontSize = (22 * fontSizeMultiplier).sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Right,
                                    lineHeight = (36 * fontSizeMultiplier).sp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                AnimatedVisibility(visible = isSelected) {
                                    Column(modifier = Modifier.padding(top = 12.dp)) {
                                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = {
                                                viewModel.toggleBookmark(
                                                    surahNum = ayah.surahNumber,
                                                    surahName = currentSurah?.arabicName ?: "",
                                                    ayahNum = ayah.ayahNumber,
                                                    pageNum = ayah.page
                                                )
                                            }) {
                                                Icon(
                                                    if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                    contentDescription = "حفظ الآية",
                                                    tint = if (isBookmarked) GoldAccent else MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            // Add personal note action
                                            Button(
                                                onClick = {
                                                    selectedAyahForAction = ayah
                                                    noteInputForSelectedAyah = ""
                                                    // Dialog prompt triggers
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("تدوين ملاحظة", fontSize = 12.sp)
                                            }
                                        }

                                        // Tafsir Area
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = "تفسير الآية (${if(tafsirMode == "saadi") "السعدي" else if(tafsirMode == "kathir") "ابن كثير" else "الميسر"}):",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = if(tafsirMode == "saadi") ayah.tafsirSaadi else if(tafsirMode == "kathir") ayah.tafsirKathir else ayah.tafsirMuyassar,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 22.sp,
                                                    textAlign = TextAlign.Justify
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        }
                    }
                }
            }

            // --- BOTTOM DRAWER / OVERLAY WIDGET FOR SELECTED INDIVIDUAL VERSE IN BOOK MODE ---
            selectedAyahForAction?.let { ayah ->
                if (isMushafMode) {
                    AlertDialog(
                        onDismissRequest = { selectedAyahForAction = null },
                        modifier = Modifier.fillMaxWidth(0.95f),
                        title = {
                            Text(
                                text = "الآية ${ayah.ayahNumber} • سورة ${currentSurah?.arabicName ?: ""}",
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = ayah.textArabic,
                                    fontSize = (20 * fontSizeMultiplier).sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Right,
                                    lineHeight = 32.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Tafsir section selector tabs inside overlay
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "تفسير الآية:",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TafsirButton("السعدي", "saadi", tafsirMode) { viewModel.selectTafsirMode("saadi") }
                                        TafsirButton("ابن كثير", "kathir", tafsirMode) { viewModel.selectTafsirMode("kathir") }
                                        TafsirButton("الميسر", "muyassar", tafsirMode) { viewModel.selectTafsirMode("muyassar") }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val tafsirContent = if(tafsirMode == "saadi") ayah.tafsirSaadi else if(tafsirMode == "kathir") ayah.tafsirKathir else ayah.tafsirMuyassar
                                Text(
                                    text = tafsirContent,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Fast Personal Reflection Note Area
                                Text(
                                    text = "تدوين تدبراتك الخاصة:",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = noteInputForSelectedAyah,
                                    onValueChange = { noteInputForSelectedAyah = it },
                                    placeholder = { Text("اكتب تلميحاتك أو تأملاتك حول الآية الكريمة لتبلغ القلوب...") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        },
                        confirmButton = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Save Notes Button
                                TextButton(
                                    onClick = {
                                        if (noteInputForSelectedAyah.isNotBlank()) {
                                            viewModel.saveNote(
                                                surahNum = ayah.surahNumber,
                                                surahName = currentSurah?.arabicName ?: "",
                                                ayahNum = ayah.ayahNumber,
                                                content = noteInputForSelectedAyah
                                            )
                                            Toast.makeText(context, "تم حفظ ملاحظتك بنجاح!", Toast.LENGTH_SHORT).show()
                                        }
                                        selectedAyahForAction = null
                                    }
                                ) {
                                    Text("حفظ وخروج", fontWeight = FontWeight.Bold)
                                }

                                // Toggle Verse Bookmark Directly
                                val isAyahBookmarked = bookmarks.any { !it.isPageBookmark && it.id == "${ayah.surahNumber}_${ayah.ayahNumber}" }
                                IconButton(
                                    onClick = {
                                        viewModel.toggleBookmark(
                                            surahNum = ayah.surahNumber,
                                            surahName = currentSurah?.arabicName ?: "",
                                            ayahNum = ayah.ayahNumber,
                                            pageNum = ayah.page
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isAyahBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = if (isAyahBookmarked) GoldAccent else MaterialTheme.colorScheme.primary
                                    )
                                }

                                TextButton(onClick = { selectedAyahForAction = null }) {
                                    Text("إلغاء", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    )
                }
            }

            // --- AUDIO RECITATION PLAYER SHEET (المشغل الصوتي للنبي) ---
            if (showAudioPlayerSheet) {
                val currentReciter by viewModel.selectedReciter.collectAsState()
                val isPlaying by viewModel.isPlayingAudio.collectAsState()
                val playbackProgress by viewModel.playbackProgress.collectAsState()
                val currentPositionText by viewModel.currentPositionText.collectAsState()
                val durationText by viewModel.durationText.collectAsState()
                val currentVolume by viewModel.audioVolume.collectAsState()
                val speed by viewModel.playSpeed.collectAsState()

                val reciters = listOf(
                    "الشيخ عبد الباسط عبد الصمد",
                    "الشيخ محمود خليل الحصري",
                    "الشيخ محمد صديق المنشاوي",
                    "الشيخ ماهر المعيقلي",
                    "الشيخ أحمد بن علي العجمي",
                    "الشيخ سعود الشريم"
                )

                ModalBottomSheet(
                    onDismissRequest = { showAudioPlayerSheet = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title Area
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المشغل الصوتي وتلاوات القراء",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right
                            )
                            IconButton(onClick = { showAudioPlayerSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Current Surah details
                        if (currentSurah != null) {
                            Text(
                                text = "تلاوة سُورَة ${currentSurah.arabicName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "عدد آياتها: ${currentSurah.numberOfAyahs} آية • ${if (currentSurah.revelationType == "Meccan") "مكية" else "مدنية"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reciter selector label
                        Text(
                            text = "اختر القارئ المفضل لديك:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Right
                        )

                        // Reciter Selection Row
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            reverseLayout = true // RTL design layout
                        ) {
                            items(reciters) { name ->
                                val isSelected = currentReciter == name
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { viewModel.selectReciter(name) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = name,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Controls Deck Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(28.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            IconButton(onClick = { viewModel.playPreviousSurah() }) {
                                Icon(
                                    Icons.Default.SkipPrevious,
                                    contentDescription = "السورة السابقة",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (viewModel.activePlayingSurahNum.value != surahNumber) {
                                        viewModel.playSurahDirectly(surahNumber)
                                    } else {
                                        viewModel.toggleAudioPlaying()
                                    }
                                },
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying && viewModel.activePlayingSurahNum.value == surahNumber) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "تشغيل التلاوة",
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            IconButton(onClick = { viewModel.playNextSurah() }) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = "السورة التالية",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        // Playback Progress slider
                        Slider(
                            value = playbackProgress,
                            onValueChange = {},
                            colors = SliderDefaults.colors(
                                thumbColor = GoldAccent,
                                activeTrackColor = GoldAccent,
                                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("playback_progress_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                currentPositionText,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                durationText,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Volume Control Slider (تحكم بالصوت)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "مستوى التلاوة",
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مستوى الصوت: ${(currentVolume * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Slider(
                                value = currentVolume,
                                onValueChange = { viewModel.setAudioVolume(it) },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldAccent,
                                    activeTrackColor = GoldAccent,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.weight(1f).testTag("audio_volume_slider")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Speed and Timer Helpers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "سرعة التشغيل: ${speed}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (speed == 1.0f) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { viewModel.setPlaySpeed(1.0f) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("1.0x", fontSize = 10.sp, color = if (speed == 1.0f) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (speed == 1.5f) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { viewModel.setPlaySpeed(1.5f) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("1.5x", fontSize = 10.sp, color = if (speed == 1.5f) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // --- DIALOG OUR WORKS / FEATURES (أعمالنا) ---
            if (showOurWorksDialog) {
                AlertDialog(
                    onDismissRequest = { showOurWorksDialog = false },
                    title = {
                        Text(
                            text = "بوابة أعمالنا وميزات التطبيق",
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        val features = remember {
                            listOf(
                                Triple("الأذكار اليومية", Icons.Default.BrightnessMedium, QuranScreen.Adhkar),
                                Triple("مواقيت الصلاة", Icons.Default.Schedule, QuranScreen.PrayerTimes),
                                Triple("اتجاه القبلة", Icons.Default.Explore, QuranScreen.Qibla),
                                Triple("التدبر والملاحظات", Icons.Default.NoteAlt, QuranScreen.Notes),
                                Triple("خطط الحفظ والمراجعة", Icons.Default.Assignment, QuranScreen.HifzPlans),
                                Triple("إحصائيات التقدم", Icons.Default.InsertChart, QuranScreen.StatisticalDashboard),
                                Triple("المساعد الذكي (AI)", Icons.Default.AutoAwesome, QuranScreen.GeminiAssistant),
                                Triple("تلاوات صوتية", Icons.Default.Headset, QuranScreen.ListenAudio)
                            )
                        }
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "تنقل سريع لجميع أعمالنا ومميزات التطبيق الإسلامي المتكامل:",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Right,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(features) { (title, icon, screen) ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                showOurWorksDialog = false
                                                viewModel.navigateTo(screen)
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                text = title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Right
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(GoldAccent.copy(alpha = 0.12f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = title,
                                                    tint = GoldAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showOurWorksDialog = false }) {
                            Text("رجوع للقرآن الكريم", color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // --- DIALOG SELECT JUZ (الجزء) ---
            if (showJuzDialog) {
                AlertDialog(
                    onDismissRequest = { showJuzDialog = false },
                    title = {
                        Text(
                            text = "اختر الجزء لقراءته",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) {
                            items((1..30).toList()) { juz ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val targetSurah = surahs.find { it.juzNumber == juz }
                                            if (targetSurah != null) {
                                                viewModel.navigateTo(QuranScreen.Reading(targetSurah.number))
                                                // Jump to start page
                                                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                                    // Load other surah instantly
                                                    viewModel.loadAyahsForSurah(targetSurah.number)
                                                }
                                            } else {
                                                Toast.makeText(context, "جاري تهيئة الجزء...", Toast.LENGTH_SHORT).show()
                                            }
                                            showJuzDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = getJuzArabicName(juz),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForwardIos,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = GoldAccent
                                    )
                                }
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showJuzDialog = false }) {
                            Text("إغلاق")
                        }
                    }
                )
            }

            // --- DIALOG SELECT PAGE (الصفحة) ---
            if (showPageDialog) {
                AlertDialog(
                    onDismissRequest = { showPageDialog = false },
                    title = {
                        Text(
                            text = "اختر الصفحة للذهاب إليها",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "صفحات سورة ${currentSurah?.arabicName ?: ""}:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                            ) {
                                itemsIndexed(pageList) { idx, pageNum ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                activePageIdx = idx
                                                showPageDialog = false
                                            }
                                            .padding(vertical = 12.dp, horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الصفحة $pageNum",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (pageNum == activePageNumber) GoldAccent else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (pageNum == activePageNumber) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = GoldAccent)
                                        }
                                    }
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPageDialog = false }) {
                            Text("إغلاق")
                        }
                    }
                )
            }

            // --- SEARCH INLINE DIALOG (البحث الفوري) ---
            if (showSearchDialog) {
                var searchQuery by remember { mutableStateOf("") }
                var searchResults by remember { mutableStateOf<List<AyahEntity>>(emptyList()) }
                var searchLoading by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showSearchDialog = false },
                    title = { Text("البحث الفوري في آيات القرآن", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { query ->
                                    searchQuery = query
                                    if (query.length > 2) {
                                        searchLoading = true
                                        scope.launch {
                                            val b = com.example.data.QuranDatabase.getDatabase(context)
                                            val r = com.example.data.QuranRepository(b.quranDao())
                                            searchResults = r.searchAyahs(query)
                                            searchLoading = false
                                        }
                                    } else {
                                        searchResults = emptyList()
                                    }
                                },
                                placeholder = { Text("اكتب كلمة قرآنيّة للبحث...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (searchLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = GoldAccent)
                                }
                            } else if (searchResults.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchQuery.length < 3) "اكتب 3 حروف على الأقل للبحث الفوري" else "لم يتم العثور على نتائج للكلمة المدخلة",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(searchResults) { result ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    // Close, go to Surah, select page
                                                    showSearchDialog = false
                                                    viewModel.navigateTo(QuranScreen.Reading(result.surahNumber))
                                                    scope.launch {
                                                        viewModel.loadAyahsForSurah(result.surahNumber)
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                val surahName = surahs.find { it.number == result.surahNumber }?.arabicName ?: ""
                                                Text(
                                                    text = "سورة $surahName • الآية ${result.ayahNumber}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = GoldAccent,
                                                    fontSize = 11.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = result.textArabic,
                                                    fontSize = 14.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSearchDialog = false }) {
                            Text("إغلاق")
                        }
                    }
                )
            }

            // --- MORE OPTIONS DIALOG (المزيد) ---
            if (showMoreMenu) {
                AlertDialog(
                    onDismissRequest = { showMoreMenu = false },
                    title = { Text("المميزات والضبط", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            // View modes selection
                            Text(
                                text = "نمط عرض صفحات السورة:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isMushafMode = true
                                        showMoreMenu = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isMushafMode) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (isMushafMode) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("المصحف الشريف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        isMushafMode = false
                                        showMoreMenu = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (!isMushafMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = if (!isMushafMode) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("تفسير الآيات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Font size Adjusters
                            Text(
                                text = "حجم خط القراءة القرآنيّة:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    viewModel.fontSizeMultiplier.value = (fontSizeMultiplier + 0.15f).coerceAtMost(2.0f)
                                }) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = "تكبير", tint = GoldAccent)
                                }
                                Text(
                                    text = "${(fontSizeMultiplier * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                IconButton(onClick = {
                                    viewModel.fontSizeMultiplier.value = (fontSizeMultiplier - 0.15f).coerceAtLeast(0.8f)
                                }) {
                                    Icon(Icons.Default.ZoomOut, contentDescription = "تصغير", tint = GoldAccent)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMoreMenu = false }) {
                            Text("رجوع للقراءة")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NavMenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = GoldAccent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFFFBF7F0).copy(alpha = 0.85f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TafsirButton(label: String, mode: String, activeMode: String, onClick: () -> Unit) {
    val isActive = mode == activeMode
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}
