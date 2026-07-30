package com.shejan.keywe.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.keywe.R
import com.shejan.keywe.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    accentColor: Color = SignalRed,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PitchBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusIndicatorDot(color = accentColor, size = 8.dp)
                    Text(
                        text = "INITIALIZATION // 0${pagerState.currentPage + 1}.03",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                }

                if (pagerState.currentPage < 2) {
                    Text(
                        text = "SKIP",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MonochromeMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onFinish() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = "ONLINE",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MatrixGreen
                    )
                }
            }

            // Pager Pages Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> OnboardingPageOne(accentColor = accentColor)
                    1 -> OnboardingPageTwo(accentColor = accentColor)
                    2 -> OnboardingPageThree(accentColor = accentColor)
                }
            }

            // Bottom Control Dock
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isCurrent = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (isCurrent) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isCurrent) accentColor else GraphiteBorder)
                        )
                    }
                }

                // Action Buttons
                if (pagerState.currentPage < 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp))
                                .clickable { onFinish() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SKIP",
                                style = DotMatrixTypography.titleMedium.copy(fontSize = 13.sp),
                                color = MonochromeMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CharcoalDark)
                                .border(1.dp, accentColor, RoundedCornerShape(6.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CONTINUE ➔",
                                style = DotMatrixTypography.titleMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                color = MonochromeWhite
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MonochromeWhite)
                            .clickable { onFinish() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusIndicatorDot(color = SignalRed, size = 8.dp)
                            Text(
                                text = "LAUNCH KEYWE ➔",
                                style = DotMatrixTypography.displayLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                color = PitchBlack
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageOne(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Radar Signal Graphic
        Box(
            modifier = Modifier
                .size(190.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.minDimension / 2f

                drawCircle(color = GraphiteBorder, radius = maxRadius * 0.4f, center = center, style = Stroke(1.dp.toPx()))
                drawCircle(color = GraphiteBorder, radius = maxRadius * 0.75f, center = center, style = Stroke(1.dp.toPx()))
                drawCircle(color = GraphiteBorder, radius = maxRadius, center = center, style = Stroke(1.2.dp.toPx()))

                val waveRadius = maxRadius * pulseProgress
                val waveAlpha = (1f - pulseProgress).coerceIn(0f, 1f)
                drawCircle(
                    color = accentColor.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(2.dp.toPx())
                )

                drawCircle(color = accentColor, radius = 5.dp.toPx(), center = center)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "DRIVERLESS CONNECTIVITY",
            style = DotMatrixTypography.displayLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = MonochromeWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ZERO HOST SOFTWARE REQUIRED",
            style = DotMatrixTypography.titleMedium.copy(fontSize = 11.sp, color = accentColor),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Platform Badges Grid
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val platforms = listOf("WINDOWS", "macOS", "LINUX", "SMART TV")
            platforms.forEach { platform ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CharcoalDark)
                        .border(1.dp, GraphiteBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = platform,
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageTwo(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "keys")
    val activeKeyIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 6,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "activeKey"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Tactile Deck Preview
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CharcoalDark)
                .border(1.dp, GraphiteBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val labels = listOf("ESC", "ALT", "WIN")
                    labels.forEachIndexed { idx, label ->
                        KeyCapBox(label = label, isHighlight = activeKeyIndex % 5 == idx, accentColor = accentColor)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val labels = listOf("CTRL", "SHIFT", "DEL")
                    labels.forEachIndexed { idx, label ->
                        KeyCapBox(label = label, isHighlight = activeKeyIndex % 5 == idx + 3, accentColor = accentColor)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PitchBlack)
                        .border(1.dp, if (activeKeyIndex >= 5) accentColor else GraphiteBorder, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MULTI-TOUCH TRACKPAD",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 9.sp),
                        color = if (activeKeyIndex >= 5) MonochromeWhite else MonochromeMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TACTILE INPUT MATRIX",
            style = DotMatrixTypography.displayLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = MonochromeWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "75% MECHANICAL DECK & TRACKPAD",
            style = DotMatrixTypography.titleMedium.copy(fontSize = 11.sp, color = accentColor),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Feature Badges Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val features = listOf("GESTURES", "F1-F12", "EMOJIS", "HAPTICS")
            features.forEach { feat ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CharcoalDark)
                        .border(1.dp, GraphiteBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = feat,
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyCapBox(label: String, isHighlight: Boolean, accentColor: Color) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isHighlight) accentColor.copy(alpha = 0.2f) else PitchBlack)
            .border(1.dp, if (isHighlight) accentColor else GraphiteBorder, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = DotMatrixTypography.titleMedium.copy(
                fontSize = 11.sp,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isHighlight) MonochromeWhite else MonochromeMuted
        )
    }
}

@Composable
private fun OnboardingPageThree(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconGlow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Showcase (Hollow Pulsing Ring & PNG Logo)
        Box(
            modifier = Modifier
                .size(190.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(glowScale)
                    .border(1.5.dp, accentColor.copy(alpha = 0.75f), CircleShape)
            )

            Image(
                painter = painterResource(id = R.drawable.keywe_icon),
                contentDescription = "Keywe Icon",
                modifier = Modifier.size(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SYSTEM READY",
            style = DotMatrixTypography.displayLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = MonochromeWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "BLUETOOTH HID PERIPHERAL",
            style = DotMatrixTypography.titleMedium.copy(fontSize = 11.sp, color = MatrixGreen),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Specs Badges Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val specs = listOf("ZERO LATENCY", "100% PRIVATE", "NATIVE HID")
            specs.forEach { spec ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CharcoalDark)
                        .border(1.dp, GraphiteBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = spec,
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                }
            }
        }
    }
}
