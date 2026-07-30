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
import androidx.compose.ui.geometry.CornerRadius
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
import kotlin.math.cos
import kotlin.math.sin

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
            // Top Status Header
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
                        text = "SYSTEM SETUP // [0${pagerState.currentPage + 1}/03]",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                }

                if (pagerState.currentPage < 2) {
                    Text(
                        text = "SKIP ✕",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = MonochromeMuted,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onFinish() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = "ONLINE",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = MatrixGreen
                    )
                }
            }

            // Pager Content
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

            // Bottom Navigation Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isCurrent = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isCurrent) 28.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
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
                                .height(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp))
                                .clickable { onFinish() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SKIP SETUP",
                                style = DotMatrixTypography.titleMedium.copy(fontSize = 14.sp),
                                color = MonochromeMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
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
                                text = "NEXT ➔",
                                style = DotMatrixTypography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                color = MonochromeWhite
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
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
                                text = "LAUNCH KEYWE SYSTEM ➔",
                                style = DotMatrixTypography.displayLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
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
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Radar Graphic
        Box(
            modifier = Modifier
                .size(220.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.minDimension / 2f

                // Static grid rings
                drawCircle(color = GraphiteBorder, radius = maxRadius * 0.33f, center = center, style = Stroke(1.dp.toPx()))
                drawCircle(color = GraphiteBorder, radius = maxRadius * 0.66f, center = center, style = Stroke(1.dp.toPx()))
                drawCircle(color = GraphiteBorder, radius = maxRadius, center = center, style = Stroke(1.5.dp.toPx()))

                // Pulsing wave
                val waveRadius = maxRadius * pulseProgress
                val waveAlpha = (1f - pulseProgress).coerceIn(0f, 1f)
                drawCircle(
                    color = accentColor.copy(alpha = waveAlpha),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(2.5.dp.toPx())
                )

                // Center node
                drawCircle(color = accentColor, radius = 6.dp.toPx(), center = center)
            }

            Text(
                text = "HID://BLE",
                style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                color = MonochromeMuted,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-4).dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "ZERO-HOST WIRELESS HID",
            style = DotMatrixTypography.displayLarge.copy(fontSize = 20.sp),
            color = MonochromeWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "OUT-OF-THE-BOX CONNECTIVITY",
            style = DotMatrixTypography.titleMedium.copy(fontSize = 12.sp, color = accentColor),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Keywe turns your device into a zero-latency hardware peripheral. Control Windows, macOS, Linux, and Smart TVs over Bluetooth with ZERO software, drivers, or scripts needed on your PC.",
            style = DotMatrixTypography.bodyMedium.copy(lineHeight = 20.sp),
            color = MonochromeMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun OnboardingPageTwo(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "keys")
    val activeKeyIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 8,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "activeKey"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Mechanical Deck Simulation
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CharcoalDark)
                .border(1.dp, GraphiteBorder, RoundedCornerShape(8.dp))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Key Row 1
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labels = listOf("ESC", "ALT", "WIN")
                    labels.forEachIndexed { idx, label ->
                        val isHighlight = activeKeyIndex % 6 == idx
                        KeyCapBox(label = label, isHighlight = isHighlight, accentColor = accentColor)
                    }
                }
                // Key Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val labels = listOf("CTRL", "SHIFT", "DEL")
                    labels.forEachIndexed { idx, label ->
                        val isHighlight = activeKeyIndex % 6 == idx + 3
                        KeyCapBox(label = label, isHighlight = isHighlight, accentColor = accentColor)
                    }
                }
                // Simulated Touchpad Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(GraphiteSubtle)
                        .border(1.dp, if (activeKeyIndex >= 6) accentColor else GraphiteBorder, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MULTI-TOUCH TRACKPAD STRIP",
                        style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                        color = if (activeKeyIndex >= 6) MonochromeWhite else MonochromeMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "TACTILE INPUT MATRIX",
            style = DotMatrixTypography.displayLarge.copy(fontSize = 20.sp),
            color = MonochromeWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "MECHANICAL DECK & GESTURES",
            style = DotMatrixTypography.titleMedium.copy(fontSize = 12.sp, color = accentColor),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Experience an authentic 75% mechanical keyboard layout, quick macro shortcuts, dynamic widescreen split-view, and customizable multi-touch gestures backed by tactile vibration pulses.",
            style = DotMatrixTypography.bodyMedium.copy(lineHeight = 20.sp),
            color = MonochromeMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun KeyCapBox(label: String, isHighlight: Boolean, accentColor: Color) {
    Box(
        modifier = Modifier
            .width(66.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isHighlight) accentColor.copy(alpha = 0.2f) else GraphiteSubtle)
            .border(1.dp, if (isHighlight) accentColor else GraphiteBorder, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = DotMatrixTypography.titleMedium.copy(
                fontSize = 12.sp,
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
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo Showcase
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing Hollow Background Ring (No Fill, Only Red Ring)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(glowScale)
                    .border(1.5.dp, accentColor.copy(alpha = 0.7f), CircleShape)
            )

            // Direct PNG Logo (No container background box)
            Image(
                painter = painterResource(id = R.drawable.keywe_icon),
                contentDescription = "Keywe Official Icon",
                modifier = Modifier.size(130.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "SYSTEM READY FOR LAUNCH",
            style = DotMatrixTypography.displayLarge.copy(fontSize = 20.sp),
            color = MonochromeWhite,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "INITIALIZATION COMPLETE",
            style = DotMatrixTypography.titleMedium.copy(fontSize = 12.sp, color = MatrixGreen),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "When ready, pair directly with your computer's standard Bluetooth settings. Your host device detects Keywe as a native hardware keyboard and touchpad instantly.",
            style = DotMatrixTypography.bodyMedium.copy(lineHeight = 20.sp),
            color = MonochromeMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}
