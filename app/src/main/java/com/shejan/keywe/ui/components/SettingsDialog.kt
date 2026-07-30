package com.shejan.keywe.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shejan.keywe.ui.theme.*

enum class AppThemePreset(val label: String, val accentColor: Color) {
    MONOCHROME_DARK("MONOCHROME RED", SignalRed),
    MATRIX_GREEN("MATRIX GREEN", MatrixGreen),
    CYBER_AMBER("CYBER AMBER", AmberWarning),
    TACTILE_WHITE("TACTILE WHITE", MonochromeWhite)
}

@Composable
fun SettingsDialog(
    currentTheme: AppThemePreset,
    onSelectTheme: (AppThemePreset) -> Unit,
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Theme, 1 = Modify, 2 = About
    var showUserGuide by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(CharcoalDark)
                .border(1.dp, GraphiteBorder, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Settings Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS & PREFERENCES",
                        style = DotMatrixTypography.titleMedium,
                        color = MonochromeWhite
                    )
                }

                // Section Navigation Bar (THEME, MODIFY, ABOUT)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TactileButton(
                        text = "THEME",
                        active = activeTab == 0,
                        accentColor = currentTheme.accentColor,
                        onClick = { activeTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TactileButton(
                        text = "MODIFY",
                        active = activeTab == 1,
                        accentColor = currentTheme.accentColor,
                        onClick = { activeTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    TactileButton(
                        text = "ABOUT",
                        active = activeTab == 2,
                        accentColor = currentTheme.accentColor,
                        onClick = { activeTab = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Active Tab Content Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (activeTab) {
                        0 -> {
                            // THEME PRESETS SECTION
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SELECT COLOR PALETTE",
                                    style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                                    color = MonochromeMuted
                                )

                                AppThemePreset.values().forEach { preset ->
                                    val isSelected = currentTheme == preset
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) PitchBlack else CharcoalDark)
                                            .border(
                                                1.dp,
                                                if (isSelected) preset.accentColor else GraphiteBorder,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSelectTheme(preset) }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            StatusIndicatorDot(color = preset.accentColor, size = 10.dp)
                                            Text(
                                                text = preset.label,
                                                style = DotMatrixTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MonochromeWhite
                                            )
                                        }
                                        if (isSelected) {
                                            Text(
                                                text = "[ ACTIVE ]",
                                                style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                                                color = preset.accentColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // MODIFY PREFERENCES SECTION
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                // Touchpad Sensitivity Modifier
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "TOUCHPAD SENSITIVITY",
                                            style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                                            color = MonochromeWhite
                                        )
                                        Text(
                                            text = String.format("%.1fx", sensitivity),
                                            style = DotMatrixTypography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            color = currentTheme.accentColor
                                        )
                                    }

                                    Slider(
                                        value = sensitivity,
                                        onValueChange = onSensitivityChange,
                                        valueRange = 0.5f..3.5f,
                                        steps = 29,
                                        colors = SliderDefaults.colors(
                                            thumbColor = currentTheme.accentColor,
                                            activeTrackColor = currentTheme.accentColor,
                                            inactiveTrackColor = GraphiteBorder
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val presets = listOf(0.8f, 1.0f, 1.5f, 2.0f, 3.0f)
                                        presets.forEach { p ->
                                            TactileButton(
                                                text = "${p}x",
                                                active = (sensitivity - p).let { kotlin.math.abs(it) < 0.05f },
                                                accentColor = currentTheme.accentColor,
                                                onClick = { onSensitivityChange(p) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Haptic Feedback Toggle Modifier
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PitchBlack)
                                        .border(1.dp, GraphiteBorder, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "HAPTIC FEEDBACK",
                                            style = DotMatrixTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MonochromeWhite
                                        )
                                        Text(
                                            text = "Tactile vibration pulse on keypress & touchpad clicks.",
                                            style = DotMatrixTypography.labelSmall.copy(fontSize = 9.5.sp),
                                            color = MonochromeMuted
                                        )
                                    }

                                    Switch(
                                        checked = hapticsEnabled,
                                        onCheckedChange = onHapticsToggle,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = PitchBlack,
                                            checkedTrackColor = currentTheme.accentColor,
                                            uncheckedThumbColor = MonochromeMuted,
                                            uncheckedTrackColor = CharcoalDark,
                                            uncheckedBorderColor = GraphiteBorder
                                        )
                                    )
                                }
                            }
                        }

                        2 -> {
                            // ABOUT SECTION
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(PitchBlack)
                                        .border(1.dp, GraphiteBorder, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "KEYWE v1.0.0",
                                            style = DotMatrixTypography.titleMedium,
                                            color = MonochromeWhite
                                        )
                                        Text(
                                            text = "NATIVE BLUETOOTH HID REMOTE CONTROLLER",
                                            style = DotMatrixTypography.labelSmall.copy(fontSize = 9.5.sp),
                                            color = currentTheme.accentColor
                                        )
                                        Text(
                                            text = "Zero host software required. Phone acts as native USB HID peripheral out-of-the-box via Bluetooth L2CAP.",
                                            style = DotMatrixTypography.bodyMedium.copy(fontSize = 11.sp),
                                            color = MonochromeMuted
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "DEVELOPER PROFILE",
                                        style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                                        color = MonochromeWhite
                                    )

                                    Text(
                                        text = "Farjan Ahmmed (Shejan)",
                                        style = DotMatrixTypography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MonochromeWhite
                                    )

                                    Text(
                                        text = "Email: farjan.swe@gmail.com",
                                        style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                                        color = MonochromeMuted
                                    )
                                }

                                TactileButton(
                                    text = "USER GUIDE // HOW TO USE",
                                    onClick = { showUserGuide = true },
                                    accentColor = currentTheme.accentColor,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    TactileButton(
                                        text = "GITHUB",
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shejanahmmed"))
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    TactileButton(
                                        text = "LINKEDIN",
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/farjan-ahmmed/"))
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    TactileButton(
                                        text = "LICENSE",
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shejanahmmed/Keywe/blob/main/LICENSE"))
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    TactileButton(
                                        text = "PRIVACY POLICY",
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.farjan.me/KeywePrivacyPolicy/"))
                                            try { context.startActivity(intent) } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.weight(1.3f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Close Button
                TactileButton(
                    text = "CLOSE",
                    onClick = onDismiss,
                    accentColor = currentTheme.accentColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showUserGuide) {
            UserGuideDialog(
                accentColor = currentTheme.accentColor,
                onDismiss = { showUserGuide = false }
            )
        }
    }
}
