@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
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
fun AdhkarScreen(viewModel: QuranViewModel) {
    var selectedCategory by remember { mutableStateOf("morning") }
    val adhkarList by viewModel.getAdhkarByCategory(selectedCategory).collectAsState(initial = emptyList())

    // Tracks counts of clicks on items during session
    val countsTracker = remember { mutableStateMapOf<Int, Int>() }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("حصن المسلم والأذكار", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(QuranScreen.Home) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = when(selectedCategory) {
                    "morning" -> 0
                    "evening" -> 1
                    "sleep" -> 2
                    else -> 3
                },
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedCategory == "morning",
                    onClick = { selectedCategory = "morning" },
                    text = { Text("أذكار الصباح", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedCategory == "evening",
                    onClick = { selectedCategory = "evening" },
                    text = { Text("أذكار المساء", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedCategory == "sleep",
                    onClick = { selectedCategory = "sleep" },
                    text = { Text("أذكار النوم", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedCategory == "prayer",
                    onClick = { selectedCategory = "prayer" },
                    text = { Text("أذكار الصلاة", fontWeight = FontWeight.Bold) }
                )
            }

            // Tasbih counter card block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {
                var tasbihCount by remember { mutableStateOf(0) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "المسبحة الإلكترونية المتنقلة",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tasbihCount.toString(),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = { tasbihCount++ },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("تسبيح (+1)", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { tasbihCount = 0 },
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "تصفير")
                        }
                    }
                }
            }

            // Adhkar List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (adhkarList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    itemsIndexed(adhkarList) { index, dhikr ->
                        val currentCount = countsTracker[dhikr.id] ?: 0
                        val isCompleted = currentCount >= dhikr.count

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (currentCount < dhikr.count) {
                                        countsTracker[dhikr.id] = currentCount + 1
                                    }
                                }
                                .testTag("dhikr_item_${dhikr.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isCompleted) Color.Gray
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "التكرار: $currentCount / ${dhikr.count}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isCompleted) Color.White else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    if (isCompleted) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "مكتمل",
                                            tint = Color.Green,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    } else {
                                        IconButton(onClick = { countsTracker[dhikr.id] = 0 }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "تصفير", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = dhikr.content,
                                    fontSize = 17.sp,
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Right,
                                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (dhikr.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = dhikr.description,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
