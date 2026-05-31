package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DhikrEntity
import com.example.ui.QuranViewModel

@Composable
fun AdhkarScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val activeDhikrs by viewModel.activeDhikrs.collectAsState()
    val selectedCategory by viewModel.selectedDhikrCategory.collectAsState()
    val tasbihCount by viewModel.tasbihCount.collectAsState()

    val context = LocalContext.current
    var isTasbihMode by remember { mutableStateOf(false) } // Toggle true = Digital Tasbih, false = Azkar list

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Top Toggle: Azkar vs Digital Tasbih
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "الأذكار اليومية",
                    isSelected = !isTasbihMode,
                    onClick = { isTasbihMode = false },
                    modifier = Modifier.weight(1f).testTag("azkar_tab_btn")
                )
                TabButton(
                    text = "السبحة الإلكترونية",
                    isSelected = isTasbihMode,
                    onClick = { isTasbihMode = true },
                    modifier = Modifier.weight(1f).testTag("tasbih_tab_btn")
                )
            }

            if (!isTasbihMode) {
                // Azkar List Section
                // Category tabs (Morning, Evening, Sleep, Post prayer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("صباح", "مساء", "نوم", "بعد الصلاة")
                    categories.forEach { cat ->
                        val isCatSelected = selectedCategory == cat
                        val badgeText = when (cat) {
                            "صباح" -> "أذكار الصباح"
                            "مساء" -> "أذكار المساء"
                            "نوم" -> "أذكار النوم"
                            "بعد الصلاة" -> "بعد الصلاة"
                            else -> cat
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isCatSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { viewModel.selectDhikrCategory(cat) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 12.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCatSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Reset category counter helper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "اضغط على الذكر لتسجيل التكرار:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    TextButton(
                        onClick = {
                            viewModel.resetDhikr(selectedCategory)
                            Toast.makeText(context, "تم تصفير العدادات للفئة بنجاح", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("reset_category_dhikrs")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصفير العداد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Lazy Column of dhikrs
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(activeDhikrs, key = { it.id }) { dhikr ->
                        DhikrRowCard(
                            dhikr = dhikr,
                            onClick = {
                                viewModel.incrementDhikr(dhikr)
                                triggerVibration(context)
                            }
                        )
                    }
                }
            } else {
                // Interactive luxurious digital tasbeeh ring
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "الْمُسَبِّحُ الْإِلِكْتُرُونِيُّ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ألا بذكر الله تطمئن القلوب",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Simulated Rosary Beads Circle (Draw beautifully)
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .clickable {
                                viewModel.clickTasbih()
                                triggerVibration(context)
                            }
                            .testTag("tasbih_counter_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing static rosary circle track
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFFB18F4E).copy(alpha = 0.4f),
                                radius = size.width / 2.3f,
                                style = Stroke(width = 8.dp.toPx())
                            )
                        }

                        // Real active counter texts
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "عَدَدُ التَّسْبِيحِ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = tasbihCount.toString(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "اضغط للتسبيح",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Controls reset button row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = {
                                viewModel.resetTasbih()
                                Toast.makeText(context, "تم تصفير المسبح بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.testTag("reset_tasbih_btn")
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصفير العداد", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DhikrRowCard(
    dhikr: DhikrEntity,
    onClick: () -> Unit
) {
    val isCompleted = dhikr.currentCount >= dhikr.targetCount
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("dhikr_${dhikr.id}_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) 
                Color(0xFFE8F5E9) // soft pastel green for completed
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 1.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Text of Dhikr
            Text(
                text = dhikr.text,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Medium,
                color = if (isCompleted) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dhikr Description (Benefit / source)
            if (dhikr.description.isNotBlank()) {
                Text(
                    text = dhikr.description,
                    fontSize = 12.sp,
                    color = if (isCompleted) Color(0xFF2E7D32).copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Divider
            Divider(color = (if (isCompleted) Color(0xFF81C784).copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant))

            Spacer(modifier = Modifier.height(8.dp))

            // Counter details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Count Visual indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) Color(0xFF2E7D32)
                                else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${dhikr.targetCount - dhikr.currentCount}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isCompleted) "تم بحمد الله تكراره" else "المتبقي من التكرار",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Progress ratio label
                Text(
                    text = "${dhikr.currentCount} / ${dhikr.targetCount}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun triggerVibration(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(45)
        }
    } catch (e: Exception) {
        // Fallback silently if vibrating is not supported
    }
}
