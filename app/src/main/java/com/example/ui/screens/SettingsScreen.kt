package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ReciterConfig
import com.example.ui.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val fontSize by viewModel.fontSize.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val selectedTafsir by viewModel.selectedTafsirType.collectAsState()
    val selectedReciter by viewModel.selectedPlayReciter.collectAsState()

    val context = LocalContext.current
    var expandedReciter by remember { mutableStateOf(false) }
    var expandedTafsir by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Title
            Text(
                text = "الإعدادات العامة للتطبيق",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Theme Switcher Section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("theme_settings_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                            imageVector = if (isDarkMode) Icons.Default.NightsStay else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "الوضع الليلي والنهاري", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = if (isDarkMode) "المظهر الليلي الأخضر داكن مفعل" else "المظهر النهاري العاجي الفاتح مفعل", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_settings_switch")
                    )
                }
            }

            // Font Resizing Section
            Card(
                modifier = Modifier.fillMaxWidth().testTag("font_settings_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "حجم خط الآيات والسور", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "تصغير", fontSize = 12.sp)
                        Slider(
                            value = fontSize,
                            onValueChange = { viewModel.setFontSize(it) },
                            valueRange = 16f..48f,
                            modifier = Modifier.weight(1f).testTag("font_size_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(text = "تكبير", fontSize = 12.sp)
                    }
                    Text(
                        text = "مثال الخط: بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }

            // Default Reciter Section Selection
            Card(
                modifier = Modifier.fillMaxWidth().testTag("reciter_settings_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedReciter = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "القارئ المفضل الافتراضي", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = selectedReciter.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = expandedReciter,
                    onDismissRequest = { expandedReciter = false }
                ) {
                    ReciterConfig.list.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r.name, fontWeight = FontWeight.Bold) },
                            onClick = {
                                viewModel.selectReciter(r)
                                expandedReciter = false
                                Toast.makeText(context, "تم تغيير القارئ الافتراضي إلى ${r.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Default Tafsir Section Selection
            Card(
                modifier = Modifier.fillMaxWidth().testTag("tafsir_settings_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedTafsir = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "التفسير الافتراضي المعتمد", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "تفسير $selectedTafsir", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = expandedTafsir,
                    onDismissRequest = { expandedTafsir = false }
                ) {
                    val tafsirs = listOf("السعدي", "ابن كثير", "الطبري")
                    tafsirs.forEach { t ->
                        DropdownMenuItem(
                            text = { Text("تفسير $t", fontWeight = FontWeight.Bold) },
                            onClick = {
                                viewModel.setTafsirType(t)
                                expandedTafsir = false
                                Toast.makeText(context, "تم تعيين التفسير الافتراضي: $t", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Backup & Restore simulation
            Card(
                modifier = Modifier.fillMaxWidth().testTag("backup_settings_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "النسخ الاحتياطي السحابي والاستعادة", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "تم رفع نسخة احتياطية من العلامات المرجعية وحالتك بنجاح ✅", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f).testTag("backup_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نسخ احتياطي", fontSize = 11.sp)
                        }

                        ElevatedButton(
                            onClick = {
                                Toast.makeText(context, "تم استعادة بيانات علاماتك وحالتك بنجاح ومزامنتها دفترياً ✅", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f).testTag("restore_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة البيانات", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
