@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun HifzPlansScreen(viewModel: QuranViewModel) {
    val hifzPlans by viewModel.hifzPlans.collectAsState()
    val surahs by viewModel.surahs.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSurahIdx by remember { mutableStateOf(0) }
    var startVerse by remember { mutableStateOf(1) }
    var endVerse by remember { mutableStateOf(10) }
    var targetDays by remember { mutableStateOf(7) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("خطط الحفظ والمراجعة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(QuranScreen.Home) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة خطة")
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة خطة جديدة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (hifzPlans.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لا توجد خطة حفظ نشطة حالياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "اضغط على زر الإضافة لإنشاء خطة حفظ أو مراجعة يومية لسورة من اختيارك لتثبيت كلام الله في صدرك.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "متابعة إنجازي اليومي للحفظ والمراجعة:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(hifzPlans) { plan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hifz_card_${plan.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "سورة ${plan.surahName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "من آية ${plan.startAyah} إلى آية ${plan.endAyah}",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    IconButton(onClick = { viewModel.deleteHifzPlan(plan) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "حذف الخطة",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Progress row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "التقدّم: ${plan.completedDays} / ${plan.targetDays} يوم",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${plan.progressPercent.toInt()}%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = plan.progressPercent / 100f,
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Quick complete action
                                if (plan.completed) {
                                    Text(
                                        text = "🎉 هنيئاً لك! لقد أكملت حفظ هذا الجزء من الخطة بنجاح.",
                                        color = GoldAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Button(
                                        onClick = { viewModel.incrementHifzPlanDay(plan) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("تسجيل إنجاز اليوم (+1)", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create new plan popup
            if (showAddDialog) {
                val surahNamesList = surahs.map { "سورة " + it.arabicName }
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text("إنشاء خطة حفظ جديدة", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (surahs.isEmpty()) {
                                Text("جاري تحميل السور...")
                            } else {
                                // Surah selection text
                                Text("اختر السورة:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    surahs.take(4).forEachIndexed { index, s ->
                                        val isSel = selectedSurahIdx == index
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if(isSel) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedSurahIdx = index }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                s.arabicName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if(isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Text("الآية البدء:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Button(onClick = { if(startVerse > 1) startVerse-- }) { Text("-") }
                                    Text(startVerse.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Button(onClick = { startVerse++ }) { Text("+") }
                                }

                                Text("الآية الانتهاء:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Button(onClick = { if(endVerse > startVerse) endVerse-- }) { Text("-") }
                                    Text(endVerse.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Button(onClick = { endVerse++ }) { Text("+") }
                                }

                                Text("مدة الخطة باليوم ($targetDays يوم):", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Slider(
                                    value = targetDays.toFloat(),
                                    onValueChange = { targetDays = it.toInt() },
                                    valueRange = 1f..30f
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (surahs.isNotEmpty()) {
                                    val s = surahs[selectedSurahIdx]
                                    viewModel.createHifzPlan(
                                        surahNum = s.number,
                                        surahName = s.arabicName,
                                        startAyah = startVerse,
                                        endAyah = endVerse,
                                        targetDays = targetDays
                                    )
                                }
                                showAddDialog = false
                            }
                        ) {
                            Text("إنشاء الخطة")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }
        }
    }
}
