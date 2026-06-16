@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.QuranScreen
import com.example.ui.QuranViewModel
import com.example.ui.theme.GoldAccent

@Composable
fun QiblaScreen(viewModel: QuranViewModel) {
    // Animate compass rotation slightly to make it look active and dynamic!
    val infiniteTransition = rememberInfiniteTransition(label = "compass")
    val compassWobble by infiniteTransition.animateFloat(
        initialValue = -2.0f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )

    // Base mock rotation value (User holding phone pointing almost North-West)
    var deviceHeadingDegrees by remember { mutableStateOf(45.0f) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("بوصلة القبلة الذكية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "مكة المكرمة الكعبة المشرفة",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "زاوية انحراف القبلة الحالية: 135° جنوب شرق",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Compass Graphic drawn completely on Canvas!
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .testTag("qibla_compass_canvas_container"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = (size.minDimension / 2.0f) * 0.85f

                    // 1. Draw outer circle
                    drawCircle(
                        color = Color(0xFF1B6E41).copy(alpha = 0.2f),
                        radius = radius,
                        center = center
                    )

                    // 2. Draw compass outer line border
                    drawCircle(
                        color = Color(0xFFCF9E2B),
                        radius = radius - 4f,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )

                    // 3. Draw North Needles rotated dynamically by device heading
                    rotate(degrees = -deviceHeadingDegrees + compassWobble, pivot = center) {
                        // North needle (N)
                        val northPath = Path().apply {
                            moveTo(center.x, center.y - radius + 15f)
                            lineTo(center.x - 12f, center.y)
                            lineTo(center.x + 12f, center.y)
                            close()
                        }
                        drawPath(northPath, color = Color.Red)

                        // South needle (S)
                        val southPath = Path().apply {
                            moveTo(center.x, center.y + radius - 15f)
                            lineTo(center.x - 12f, center.y)
                            lineTo(center.x + 12f, center.y)
                            close()
                        }
                        drawPath(southPath, color = Color.LightGray)
                    }

                    // 4. Draw Kaaba Needle pointing permanently to 135 degrees!
                    // Rotated also based on device heading so it changes relative to direction of phone!
                    val finalKaabaAngle = (135f - deviceHeadingDegrees) + compassWobble
                    rotate(degrees = finalKaabaAngle, pivot = center) {
                        // Kaaba Golden Needle
                        val kaabaPath = Path().apply {
                            moveTo(center.x, center.y - radius + 30f)
                            lineTo(center.x - 16f, center.y)
                            lineTo(center.x + 16f, center.y)
                            close()
                        }
                        drawPath(kaabaPath, color = Color(0xFFCF9E2B))

                        // Small Kaaba center circle
                        drawCircle(
                            color = Color.Black,
                            radius = 16f,
                            center = center
                        )

                        // Glowing gold center dot
                        drawCircle(
                            color = Color(0xFFFFD700),
                            radius = 6f,
                            center = center
                        )
                    }
                }
            }

            // Slider to let the user simulate turning the phone around!
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "قم بتدوير الهاتف يدوياً لمحاكاة الإشارة:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Slider(
                    value = deviceHeadingDegrees,
                    onValueChange = { deviceHeadingDegrees = it },
                    valueRange = 0f..360f,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                )
                Text(
                    text = "اتجاه الهاتف الحالي: ${deviceHeadingDegrees.toInt()}°",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Status message card
            val aligned = (deviceHeadingDegrees >= 130f && deviceHeadingDegrees <= 140f)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (aligned) Color(0xFF1B6E41).copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = if (aligned) Color.Green else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (aligned) "أنت تواجه القبلة الآن بدقة! وجه وجهك شطر المسجد الحرام."
                        else "قم بتحريك وتدوير جهازك حتى يستقر المؤشر الذهبي في المنتصف الأعلى تماماً.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
