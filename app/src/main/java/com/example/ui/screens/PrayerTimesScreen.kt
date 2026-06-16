@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
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
fun PrayerTimesScreen(viewModel: QuranViewModel) {
    var selectedCity by remember { mutableStateOf("القاهرة، مصر") }
    val cities = listOf("القاهرة، مصر", "مكة المكرمة، السعودية", "القدس الشريف، فلسطين", "الإسكندرية، مصر", "دبي، الإمارات")
    var isMutedMap = remember { mutableStateMapOf<String, Boolean>() }

    // Hardcoded highly precise prayer times for mock/interactive display
    val timesMap = mapOf(
        "القاهرة، مصر" to listOf("الفجر" to "03:15 ص", "الشروق" to "05:03 ص", "الظهر" to "11:58 ص", "العصر" to "03:32 م", "المغرب" to "06:54 م", "العشاء" to "08:31 م"),
        "مكة المكرمة، السعودية" to listOf("الفجر" to "04:12 ص", "الشروق" to "05:40 ص", "الظهر" to "12:22 م", "العصر" to "03:41 م", "المغرب" to "07:01 م", "العشاء" to "08:31 م"),
        "القدس الشريف، فلسطين" to listOf("الفجر" to "03:48 ص", "الشروق" to "05:32 ص", "الظهر" to "12:38 م", "العصر" to "04:14 م", "المغرب" to "07:44 م", "العشاء" to "09:12 م"),
        "الإسكندرية، مصر" to listOf("الفجر" to "03:18 ص", "الشروق" to "05:08 ص", "الظهر" to "12:03 م", "العصر" to "03:40 م", "المغرب" to "07:01 م", "العشاء" to "08:41 م"),
        "دبي، الإمارات" to listOf("الفجر" to "04:02 ص", "الشروق" to "05:28 ص", "الظهر" to "12:14 م", "العصر" to "03:36 م", "المغرب" to "06:58 م", "العشاء" to "08:24 م")
    )

    val currentTimes = timesMap[selectedCity] ?: timesMap.values.first()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("مواقيت الصلاة اليومية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
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
            // City Selector Horizontal List
            item {
                Column {
                    Text(
                        text = "اختر المدينة لحساب المواقيت:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cities.take(3).forEach { city ->
                            val isSelected = selectedCity == city
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedCity = city }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = city.substringBefore("،"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Headliner countdown (Next Prayer)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("next_prayer_countdown_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = selectedCity, color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "الصلاة القادمة: صلاة العصر",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "المتبقي: 02:44:12",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // Individual times entries list
            item {
                Text(
                    text = "أوقات الصلاة حسب التقويم الهجري والمحلي:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(currentTimes.size) { index ->
                val (prayerName, prayerTime) = currentTimes[index]
                val isNext = prayerName == "العصر" // Mock currently highlighted next prayer
                val isNotifyEnabled = !(isMutedMap[prayerName] ?: false)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isNext) 2.dp else 0.dp,
                            color = if (isNext) GoldAccent else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNext) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
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
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isNext) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    color = if (isNext) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = prayerName,
                                fontSize = 17.sp,
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = prayerTime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNext) GoldAccent else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = { isMutedMap[prayerName] = isNotifyEnabled }) {
                                Icon(
                                    imageVector = if (isNotifyEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = "تنبيه الأذان",
                                    tint = if (isNotifyEnabled) GoldAccent else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
