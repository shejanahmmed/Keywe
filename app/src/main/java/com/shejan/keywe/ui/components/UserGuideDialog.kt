package com.shejan.keywe.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shejan.keywe.ui.theme.*

@Composable
fun UserGuideDialog(
    accentColor: Color = SignalRed,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(14.dp))
                .background(PitchBlack)
                .border(1.dp, GraphiteBorder, RoundedCornerShape(14.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header (Clean Title)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusIndicatorDot(color = accentColor, size = 8.dp)
                    Text(
                        text = "KEYWE USER GUIDE",
                        style = DotMatrixTypography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = MonochromeWhite
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Step Cards Container
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Step 1: Bluetooth Pairing
                    GuideCard(
                        step = "01",
                        title = "BLUETOOTH PAIRING",
                        subtitle = "Zero-Host Driverless Connection",
                        accentColor = accentColor,
                        iconGraphic = { BluetoothIconGraphic(tint = accentColor) },
                        bulletPoints = listOf(
                            "Turn on Bluetooth on your phone and PC / Mac / Smart TV.",
                            "Tap the LED status badge in the top header to open Device Manager.",
                            "Select your target computer under PAIRED or search under NEARBY.",
                            "Keywe connects instantly as a native hardware Bluetooth device — zero PC software required!"
                        )
                    )

                    // Step 2: Trackpad & Multi-Touch Gestures
                    GuideCard(
                        step = "02",
                        title = "MULTI-TOUCH TRACKPAD",
                        subtitle = "Precision Pointer & Gestures",
                        accentColor = accentColor,
                        iconGraphic = { TrackpadIconGraphic(tint = accentColor) },
                        bulletPoints = listOf(
                            "1-Finger Drag: Relative precision cursor movement.",
                            "1-Finger Tap: Left Click.",
                            "2-Finger Tap: Right Click.",
                            "2-Finger Swipe: Smooth vertical scrolling and horizontal panning.",
                            "Integrated Click Bar: Dedicated [ L-CLICK ] and [ R-CLICK ] zones built into trackpad bottom."
                        )
                    )

                    // Step 3: 75% Mechanical Keyboard
                    GuideCard(
                        step = "03",
                        title = "75% MECHANICAL DECK",
                        subtitle = "Tactile Keycaps & Hotkeys",
                        accentColor = accentColor,
                        iconGraphic = { KeyboardIconGraphic(tint = accentColor) },
                        bulletPoints = listOf(
                            "Full 75% Layout: Function row (F1-F12), Esc (Red), PrtSc, Del, Enter, and Arrow cluster.",
                            "Modifier Shortcuts: Tap Ctrl or Win to reveal dynamic shortcut labels (e.g., COPY, PASTE, UNDO).",
                            "One-Shot Shift: Auto-releases after each keystroke for natural typing.",
                            "Mode Switcher: Toggle between TOUCHPAD, TACTILE DECK, and SPLIT VIEW."
                        )
                    )

                    // Step 4: Soft Keyboard & Emoji Input
                    GuideCard(
                        step = "04",
                        title = "SOFT KEYBOARD & EMOJIS",
                        subtitle = "Phone IME & Quick Line Breaks",
                        accentColor = accentColor,
                        iconGraphic = { EmojiIconGraphic(tint = accentColor) },
                        bulletPoints = listOf(
                            "System Soft Keyboard: Long-press [ KEYBOARD ] to type using Gboard, SwiftKey, or Voice Typing.",
                            "Line Break: Tap LINE BREAK to jump to the next line on your computer.",
                            "Emoji Strip: Scroll and tap any emoji to insert it directly into your text field."
                        )
                    )

                    // Step 5: Customization & Preferences
                    GuideCard(
                        step = "05",
                        title = "SETTINGS & PREFERENCES",
                        subtitle = "Sensitivity & Themes",
                        accentColor = accentColor,
                        iconGraphic = { SettingsIconGraphic(tint = accentColor) },
                        bulletPoints = listOf(
                            "Orientation: Tap rotation button in header for Portrait or Widescreen Landscape.",
                            "Touchpad Speed: Adjust sensitivity slider (0.5x - 3.5x) in Settings.",
                            "Color Themes: Switch between 4 palettes (Monochrome Red, Matrix Green, Cyber Amber, Tactile White)."
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Button
                TactileButton(
                    text = "CLOSE USER GUIDE",
                    onClick = onDismiss,
                    accentColor = accentColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun GuideCard(
    step: String,
    title: String,
    subtitle: String,
    bulletPoints: List<String>,
    accentColor: Color,
    iconGraphic: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalDark)
            .border(1.dp, GraphiteBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 1. Top STEP Badge Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "STEP $step",
                    style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = MonochromeWhite,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // 2. Section Header Row (Icon + Title Side-by-Side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon Graphic Box
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(PitchBlack)
                        .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    iconGraphic()
                }

                // Section Title Text
                Text(
                    text = title,
                    style = DotMatrixTypography.titleMedium.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Bold),
                    color = MonochromeWhite
                )
            }

            // 3. Subtitle Row
            Text(
                text = subtitle.uppercase(),
                style = DotMatrixTypography.labelSmall.copy(fontSize = 9.5.sp),
                color = accentColor
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GraphiteSubtle)
            )

            // Bullet Points List
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                bulletPoints.forEach { point ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = DotMatrixTypography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                            color = accentColor
                        )
                        Text(
                            text = point,
                            style = DotMatrixTypography.bodyMedium.copy(fontSize = 11.5.sp, lineHeight = 16.5.sp),
                            color = MonochromeMuted
                        )
                    }
                }
            }
        }
    }
}

// Custom 2D Graphics for Guide Icons

@Composable
private fun BluetoothIconGraphic(tint: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = tint, radius = size.minDimension * 0.45f, center = center, style = Stroke(1.2.dp.toPx()))
        drawCircle(color = tint, radius = 2.dp.toPx(), center = center)
    }
}

@Composable
private fun TrackpadIconGraphic(tint: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        drawRoundRect(
            color = tint,
            size = Size(size.width, size.height * 0.75f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            style = Stroke(1.2.dp.toPx())
        )
        drawLine(
            color = tint,
            start = Offset(size.width / 2f, size.height * 0.5f),
            end = Offset(size.width / 2f, size.height * 0.75f),
            strokeWidth = 1.2.dp.toPx()
        )
    }
}

@Composable
private fun KeyboardIconGraphic(tint: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width / 3.2f
        val h = size.height / 3.2f
        drawRect(color = tint, topLeft = Offset(0f, 0f), size = Size(w, h))
        drawRect(color = tint, topLeft = Offset(w * 1.2f, 0f), size = Size(w, h))
        drawRect(color = tint, topLeft = Offset(w * 2.4f, 0f), size = Size(w, h))
        drawRect(color = tint, topLeft = Offset(0f, h * 1.4f), size = Size(size.width, h))
    }
}

@Composable
private fun EmojiIconGraphic(tint: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = tint, radius = size.minDimension * 0.42f, center = center, style = Stroke(1.2.dp.toPx()))
        drawCircle(color = tint, radius = 1.2.dp.toPx(), center = Offset(size.width * 0.35f, size.height * 0.38f))
        drawCircle(color = tint, radius = 1.2.dp.toPx(), center = Offset(size.width * 0.65f, size.height * 0.38f))
    }
}

@Composable
private fun SettingsIconGraphic(tint: Color) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = tint, radius = size.minDimension * 0.42f, center = center, style = Stroke(1.2.dp.toPx()))
        drawCircle(color = tint, radius = size.minDimension * 0.18f, center = center)
    }
}
