@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
fun StatisticalDashboardScreen(viewModel: QuranViewModel) {
    val statsList by viewModel.recentStats.collectAsState()

    // Mock dummy datasets in case user has no local accumulated histories yet
    val mockStats = listOf(
        "ن" to 3,
        "ث" to 8,
        "ر" to 5,
        "خ" to 12,
        "ج" to 6,
        "س" to 15,
        "أ" to 9
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("إنجازاتي القرآنية البيانية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
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
            
            // Total Metrics Header Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCounterCard("الصفحات", "📖 58 صفحه", Modifier.weight(1f))
                    MetricCounterCard("الختمات", "🏆 1 ختمه", Modifier.weight(1f))
                    MetricCounterCard("الاستماع", "🎧 120 دقيقه", Modifier.weight(1f))
                }
            }

            // Stat section description
            item {
                Column {
                    Text(
                        text = "معدّل القراءة اليومي (الصفحات):",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "يوضح المخطط البياني التالي التزامك وعدد الصفحات التي تمت قراءتها على مدار الأسبوع.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Custom statistical drawing using Compase Canvas!!
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .testTag("stats_chart_container"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "تحليل النشاط الأسبوعي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Icon(Icons.Default.BarChart, contentDescription = null, tint = GoldAccent)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Drawing on Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            val spacing = size.width / (mockStats.size + 1)
                            val canvasHeight = size.height
                            val maxPages = 18f // Normalize to maximum page scale

                            // Draw baseline grid support line
                            drawLine(
                                color = Color.LightGray.copy(alpha = 0.5f),
                                start = Offset(0f, canvasHeight - 30f),
                                end = Offset(size.width, canvasHeight - 30f),
                                strokeWidth = 2f
                            )

                            mockStats.forEachIndexed { idx, (dayChar, pagesVal) ->
                                val xOffset = spacing * (idx + 1)
                                val barWidth = 34f
                                val barHeight = ((pagesVal / maxPages) * (canvasHeight - 60f)).coerceAtLeast(10f)
                                val yOffset = canvasHeight - 30f - barHeight

                                // Draw the dynamic gold bar representing pages read
                                drawRect(
                                    color = if (idx == 5) Color(0xFF1B6E41) else Color(0xFFCF9E2B), // highlight highest achievement day
                                    size = Size(barWidth, barHeight),
                                    topLeft = Offset(xOffset - (barWidth / 2f), yOffset)
                                )

                                // Draw values on top of the bars
                                // (We can simulate texts, but since drawing text on raw DrawScope can be complex with typeface setups,
                                // we specify clean visual nodes)
                            }
                        }

                        // Day labels
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            mockStats.forEach { (dayChar, _) ->
                                Text(
                                    text = dayChar,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Accomplishments lists
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 نصيحة لتحسين وتثبيت القراءة:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "لقد حافظت على الورد اليومي بمعدل 8 صفحات يومياً! ننصحك بزيادة الورد بمقدار صفحتين غداً للوصول للمعدّل الممتاز والمختبر للحفظ السريع مع التفسير.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCounterCard(label: String, valText: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(valText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        }
    }
}
