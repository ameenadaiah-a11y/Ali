@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel
import com.example.ui.theme.GoldAccent

@Composable
fun SettingsScreen(viewModel: QuranViewModel) {
    val isDarkValue = viewModel.isDarkThemeValue.value

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("إعدادات التطبيق والتخصيص", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Theme setting block
            Card(
                modifier = Modifier.fillMaxWidth().testTag("app_themes_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("مظهر التطبيق (الوضع الداكن والفاتح):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionBtn("الوضع التلقائي", isDarkValue == null, Modifier.weight(1f)) {
                            viewModel.isDarkThemeValue.value = null
                        }
                        ThemeOptionBtn("الوضع الداكن", isDarkValue == true, Modifier.weight(1f)) {
                            viewModel.isDarkThemeValue.value = true
                        }
                        ThemeOptionBtn("الوضع الفاتح", isDarkValue == false, Modifier.weight(1f)) {
                            viewModel.isDarkThemeValue.value = false
                        }
                    }
                }
            }

            // Typography sizes adjustment block
            Card(
                modifier = Modifier.fillMaxWidth().testTag("typography_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TextFormat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("ضبط حجم خط السور والآيات:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Slider(
                        value = viewModel.fontSizeMultiplier.value,
                        onValueChange = { viewModel.fontSizeMultiplier.value = it },
                        valueRange = 0.8f..2.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("صغير جداً", fontSize = 11.sp, color = Color.Gray)
                        Text("افتراضي (${"%.1f".format(viewModel.fontSizeMultiplier.value)}x)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("كبير جداً", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            // Download All Real Holy Quran Chapters Block
            val isDownloading = viewModel.isDownloadingRealQuran.collectAsState().value
            val progressString = viewModel.downloadProgressString.collectAsState().value

            Card(
                modifier = Modifier.fillMaxWidth().testTag("download_all_quran_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("تحميل سور القرآن الكريم الحقيقية بالكامل:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isDownloading) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = GoldAccent)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = progressString,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.downloadAllRealSurahs() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("تنزيل الـ 114 سورة كواجهة موثقة حقيقية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "هذه الخطوة تقوم بجلب السور الحقيقية كاملة بالتفصيل والتفاسير المعتمدة من خوادم (api.alquran.cloud) آلياً لحفظها أوفلاين في التطبيق للأبد.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            // About application credentials / specifications block
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📜 معلومات الترخيص والمصادر المعتمدة:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• خطوط الرسم العثماني مستوحاة من مجمع الملك فهد لطباعة المصحف الشريف بـ المدينة المنورة.\n• التفاسير المعتمدة بالتطبيق من كتب: تفسير السعدي (تيسير الكريم الرحمن)، تفسير ابن كثير، والتفسير الميسر.\n• تطبيق القرآن الكريم الإصدار 1.0.0. آمن وموثوق ومبني باستخدام معايير أندرويد الحديثة ومحمي بالكامل.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeOptionBtn(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}
