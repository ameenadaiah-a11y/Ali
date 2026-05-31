package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.data.CityConfig
import com.example.data.PrayerTimesCalculator
import com.example.ui.QuranViewModel
import kotlin.math.abs

@Composable
fun PrayerCompassScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val times by viewModel.activePrayerTimes.collectAsState()
    val qiblaAngle by viewModel.qiblaAngle.collectAsState()
    val azimuth by viewModel.azimuth.collectAsState()
    val selectedCityIndex by viewModel.selectedCityIndex.collectAsState()

    val context = LocalContext.current
    var isCompassMode by remember { mutableStateOf(false) } // Toggle false = Prayer Times table, true = Compass pointer
    var showCityDropdown by remember { mutableStateOf(false) }

    // Compass dial orientation is opposite to azimuth
    val dialRotation = -azimuth
    // Relative difference from aligned qibla
    val diffAngle = (azimuth - qiblaAngle + 360) % 360
    val isAligned = abs(diffAngle) < 5.0 || abs(diffAngle - 360) < 5.0

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Dropdown Selector for Cities (Coordinates update)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("location_selector_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCityDropdown = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "الموقع الحالي لتحديد المواقيت:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = times.cityName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "اختر مدينة")
                }

                // Cities dropdown expander
                DropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false }
                ) {
                    PrayerTimesCalculator.predefinedCities.forEachIndexed { index, city ->
                        DropdownMenuItem(
                            text = { Text(city.name, fontWeight = FontWeight.Bold) },
                            onClick = {
                                viewModel.setCityIndex(index)
                                showCityDropdown = false
                                Toast.makeText(context, "تم تغيير الموقع إلى ${city.name}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // High Fidelity Toggle: Daily Times vs Qibla Compass
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                TabButton(
                    text = "مواقيت الصلاة",
                    isSelected = !isCompassMode,
                    onClick = { isCompassMode = false },
                    modifier = Modifier.weight(1f).testTag("times_tab_btn")
                )
                TabButton(
                    text = "اتجاه القبلة",
                    isSelected = isCompassMode,
                    onClick = { isCompassMode = true },
                    modifier = Modifier.weight(1f).testTag("compass_tab_btn")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isCompassMode) {
                // Table of Prayer Times
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "مواقيت الفريضة لليوم:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    val prayerRows = listOf(
                        PrayerRowData("الفجر", times.fajr, Icons.Default.WbTwilight),
                        PrayerRowData("الشروق", times.sunrise, Icons.Default.WbSunny),
                        PrayerRowData("الظهر", times.dhuhr, Icons.Default.LightMode),
                        PrayerRowData("العصر", times.asr, Icons.Default.FilterDrama),
                        PrayerRowData("المغرب", times.maghrib, Icons.Default.NightlightRound),
                        PrayerRowData("العشاء", times.isha, Icons.Default.NightsStay)
                    )

                    prayerRows.forEach { row ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("prayer_${row.name}_card"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                        imageVector = row.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = row.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = row.time,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                // Qibla rotating compass drawing
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "بَوْصَلَةُ الْقِبْلَةِ الشَّرِيفَةِ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "قُم بتوجيه الهاتف ببطء حتى تتماشى الإشارة مع الكعبة",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Alignment green/red banner
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isAligned) Color(0xFFD4EDDA) else MaterialTheme.colorScheme.surface
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("alignment_banner")
                    ) {
                        Text(
                            text = if (isAligned) "✅ أنت الآن باتجاه الكعبة الشريفة!" else "انعطف حتى تتم محاذاة المؤشر",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) Color(0xFF155724) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Beautiful Rotating compass ring area
                    Box(
                        modifier = Modifier
                            .size(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background dials rotating
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(dialRotation)
                                .testTag("compass_ring")
                        ) {
                            val goldColor = Color(0xFFC5A059)
                            val darkGreen = Color(0xFF0F5A2C)
                            
                            // Compass Circular Frame
                            drawCircle(
                                color = goldColor,
                                radius = size.width / 2.1f,
                                style = Stroke(width = 4.dp.toPx())
                            )
                            // Outer notches compass
                            for (angle in 0 until 360 step 30) {
                                val angleRad = Math.toRadians(angle.toDouble())
                                val xOuter = (size.width / 2f + (size.width / 2.1f * Math.cos(angleRad)).toFloat())
                                val yOuter = (size.height / 2f + (size.height / 2.1f * Math.sin(angleRad)).toFloat())
                                val xInner = (size.width / 2f + ((size.width / 2.1f - 8.dp.toPx()) * Math.cos(angleRad)).toFloat())
                                val yInner = (size.height / 2f + ((size.height / 2.1f - 8.dp.toPx()) * Math.sin(angleRad)).toFloat())
                                drawLine(color = goldColor, start = androidx.compose.ui.geometry.Offset(xOuter, yOuter), end = androidx.compose.ui.geometry.Offset(xInner, yInner), strokeWidth = 2.dp.toPx())
                            }
                        }

                        // Qibla Pointer showing actual angle from user coordinates to Kaaba
                        // (Takes local compass direction Dial offset into account so it turns dynamically)
                        val relativePointerRotation = dialRotation + qiblaAngle.toFloat()
                        
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .rotate(relativePointerRotation),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pointing Arrow Vector
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val pointerColor = if (isAligned) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                                val w = size.width
                                val h = size.height
                                
                                // Draw an elegant triangle pointer towards TOP (0 degrees = Qibla)
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w / 2f, 16.dp.toPx()) // Arrow tip (top)
                                    lineTo(w / 2f - 14.dp.toPx(), h / 2.4f)
                                    lineTo(w / 2f + 14.dp.toPx(), h / 2.4f)
                                    close()
                                }
                                drawPath(path, color = pointerColor)

                                // Center decorative Kaaba box representation
                                drawRect(
                                    color = Color.Black,
                                    topLeft = androidx.compose.ui.geometry.Offset(w / 2f - 16.dp.toPx(), h / 2f - 16.dp.toPx()),
                                    size = androidx.compose.ui.geometry.Size(32.dp.toPx(), 32.dp.toPx())
                                )
                                // Golden line on Kaaba
                                drawLine(
                                    color = Color(0xFFFFD700),
                                    start = androidx.compose.ui.geometry.Offset(w / 2f - 16.dp.toPx(), h / 2f - 6.dp.toPx()),
                                    end = androidx.compose.ui.geometry.Offset(w / 2f + 16.dp.toPx(), h / 2f - 6.dp.toPx()),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Angle indicators info
                    Text(
                        text = "زاوية القبلة: ${qiblaAngle.toInt()}° • اتجاه الهاتف: ${azimuth.toInt()}°",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class PrayerRowData(
    val name: String,
    val time: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
