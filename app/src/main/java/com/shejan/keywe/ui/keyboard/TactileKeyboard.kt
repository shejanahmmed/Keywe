package com.shejan.keywe.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.keywe.bt.HidReportDescriptor
import com.shejan.keywe.ui.theme.*

/**
 * Standard USB HID Usage Keycodes for Keyboard.
 */
object KeycodeMap {
    const val KEY_A: Byte = 0x04
    const val KEY_B: Byte = 0x05
    const val KEY_C: Byte = 0x06
    const val KEY_D: Byte = 0x07
    const val KEY_E: Byte = 0x08
    const val KEY_F: Byte = 0x09
    const val KEY_G: Byte = 0x0A
    const val KEY_H: Byte = 0x0B
    const val KEY_I: Byte = 0x0C
    const val KEY_J: Byte = 0x0D
    const val KEY_K: Byte = 0x0E
    const val KEY_L: Byte = 0x0F
    const val KEY_M: Byte = 0x10
    const val KEY_N: Byte = 0x11
    const val KEY_O: Byte = 0x12
    const val KEY_P: Byte = 0x13
    const val KEY_Q: Byte = 0x14
    const val KEY_R: Byte = 0x15
    const val KEY_S: Byte = 0x16
    const val KEY_T: Byte = 0x17
    const val KEY_U: Byte = 0x18
    const val KEY_V: Byte = 0x19
    const val KEY_W: Byte = 0x1A
    const val KEY_X: Byte = 0x1B
    const val KEY_Y: Byte = 0x1C
    const val KEY_Z: Byte = 0x1D

    const val KEY_1: Byte = 0x1E
    const val KEY_2: Byte = 0x1F
    const val KEY_3: Byte = 0x20
    const val KEY_4: Byte = 0x21
    const val KEY_5: Byte = 0x22
    const val KEY_6: Byte = 0x23
    const val KEY_7: Byte = 0x24
    const val KEY_8: Byte = 0x25
    const val KEY_9: Byte = 0x26
    const val KEY_0: Byte = 0x27

    const val KEY_ENTER: Byte = 0x28
    const val KEY_ESCAPE: Byte = 0x29
    const val KEY_BACKSPACE: Byte = 0x2A
    const val KEY_TAB: Byte = 0x2B
    const val KEY_SPACE: Byte = 0x2C
    const val KEY_MINUS: Byte = 0x2D
    const val KEY_EQUAL: Byte = 0x2E
    const val KEY_LEFTBRACE: Byte = 0x2F
    const val KEY_RIGHTBRACE: Byte = 0x30
    const val KEY_BACKSLASH: Byte = 0x31
    const val KEY_SEMICOLON: Byte = 0x33
    const val KEY_APOSTROPHE: Byte = 0x34
    const val KEY_GRAVE: Byte = 0x35
    const val KEY_COMMA: Byte = 0x36
    const val KEY_DOT: Byte = 0x37
    const val KEY_SLASH: Byte = 0x38
    const val KEY_CAPSLOCK: Byte = 0x39

    const val KEY_F1: Byte = 0x3A
    const val KEY_F2: Byte = 0x3B
    const val KEY_F3: Byte = 0x3C
    const val KEY_F4: Byte = 0x3D
    const val KEY_F5: Byte = 0x3E
    const val KEY_F6: Byte = 0x3F
    const val KEY_F7: Byte = 0x40
    const val KEY_F8: Byte = 0x41
    const val KEY_F9: Byte = 0x42
    const val KEY_F10: Byte = 0x43
    const val KEY_F11: Byte = 0x44
    const val KEY_F12: Byte = 0x45

    const val KEY_DELETE: Byte = 0x4C
    const val KEY_RIGHT_ARROW: Byte = 0x4F
    const val KEY_LEFT_ARROW: Byte = 0x50
    const val KEY_DOWN_ARROW: Byte = 0x51
    const val KEY_UP_ARROW: Byte = 0x52
}

private val NUM_KEYS = listOf(
    "1" to KeycodeMap.KEY_1, "2" to KeycodeMap.KEY_2, "3" to KeycodeMap.KEY_3,
    "4" to KeycodeMap.KEY_4, "5" to KeycodeMap.KEY_5, "6" to KeycodeMap.KEY_6,
    "7" to KeycodeMap.KEY_7, "8" to KeycodeMap.KEY_8, "9" to KeycodeMap.KEY_9,
    "0" to KeycodeMap.KEY_0
)

private val QWERTY_ROW_1 = listOf(
    "q" to KeycodeMap.KEY_Q, "w" to KeycodeMap.KEY_W, "e" to KeycodeMap.KEY_E,
    "r" to KeycodeMap.KEY_R, "t" to KeycodeMap.KEY_T, "y" to KeycodeMap.KEY_Y,
    "u" to KeycodeMap.KEY_U, "i" to KeycodeMap.KEY_I, "o" to KeycodeMap.KEY_O,
    "p" to KeycodeMap.KEY_P
)

private val QWERTY_ROW_2 = listOf(
    "a" to KeycodeMap.KEY_A, "s" to KeycodeMap.KEY_S, "d" to KeycodeMap.KEY_D,
    "f" to KeycodeMap.KEY_F, "g" to KeycodeMap.KEY_G, "h" to KeycodeMap.KEY_H,
    "j" to KeycodeMap.KEY_J, "k" to KeycodeMap.KEY_K, "l" to KeycodeMap.KEY_L
)

private val QWERTY_ROW_3 = listOf(
    "z" to KeycodeMap.KEY_Z, "x" to KeycodeMap.KEY_X, "c" to KeycodeMap.KEY_C,
    "v" to KeycodeMap.KEY_V, "b" to KeycodeMap.KEY_B, "n" to KeycodeMap.KEY_N,
    "m" to KeycodeMap.KEY_M
)

@Composable
fun TactileKeyboard(
    onSendKey: (modifiers: Byte, keycode: Byte) -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 44.dp,
    accentColor: Color = SignalRed,
    hapticsEnabled: Boolean = true
) {
    val hapticFeedback = androidx.compose.ui.platform.LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val vibrator = remember(context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
        }
    }

    fun triggerHapticPulse() {
        if (!hapticsEnabled) return
        try {
            hapticFeedback.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            if (vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(25L, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(25L)
                }
            }
        } catch (_: Exception) {}
    }

    var ctrlActive by remember { mutableStateOf(false) }
    var altActive by remember { mutableStateOf(false) }
    var shiftActive by remember { mutableStateOf(false) }
    var winActive by remember { mutableStateOf(false) }
    var capsActive by remember { mutableStateOf(false) }

    fun currentModifiers(): Byte {
        var mods = 0
        if (ctrlActive) mods = mods or HidReportDescriptor.MODIFIER_LEFT_CTRL.toInt()
        if (altActive) mods = mods or HidReportDescriptor.MODIFIER_LEFT_ALT.toInt()
        if (shiftActive) mods = mods or HidReportDescriptor.MODIFIER_LEFT_SHIFT.toInt()
        if (winActive) mods = mods or HidReportDescriptor.MODIFIER_LEFT_GUI.toInt()
        return mods.toByte()
    }

    fun pressKey(keycode: Byte) {
        triggerHapticPulse()
        val mods = currentModifiers()
        onSendKey(mods, keycode)
        onSendKey(mods, 0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PitchBlack)
            .border(1.dp, GraphiteBorder, RoundedCornerShape(8.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Row 0: Function Keys (Red ESC + F1-F12 + Delete)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            KeyCap(
                label = "Esc",
                height = keyHeight,
                fontSize = 10.sp,
                customBg = accentColor,
                customFg = MonochromeWhite,
                modifier = Modifier.weight(1.1f)
            ) { pressKey(KeycodeMap.KEY_ESCAPE) }

            val fKeys = listOf(
                "F1" to KeycodeMap.KEY_F1, "F2" to KeycodeMap.KEY_F2, "F3" to KeycodeMap.KEY_F3, "F4" to KeycodeMap.KEY_F4,
                "F5" to KeycodeMap.KEY_F5, "F6" to KeycodeMap.KEY_F6, "F7" to KeycodeMap.KEY_F7, "F8" to KeycodeMap.KEY_F8,
                "F9" to KeycodeMap.KEY_F9, "F10" to KeycodeMap.KEY_F10, "F11" to KeycodeMap.KEY_F11, "F12" to KeycodeMap.KEY_F12
            )
            for ((label, code) in fKeys) {
                KeyCap(
                    label = label,
                    height = keyHeight,
                    fontSize = 9.sp,
                    customBg = CharcoalDark,
                    modifier = Modifier.weight(1f)
                ) { pressKey(code) }
            }

            KeyCap(
                label = "Delete",
                height = keyHeight,
                fontSize = 8.5.sp,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.2f)
            ) { pressKey(KeycodeMap.KEY_DELETE) }
        }

        // Row 1: Numbers & Symbols
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            KeyCap(label = "`", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_GRAVE) }
            for ((label, code) in NUM_KEYS) {
                KeyCap(label = label, height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(code) }
            }
            KeyCap(label = "-", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_MINUS) }
            KeyCap(label = "=", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_EQUAL) }
            KeyCap(
                label = "Backspace",
                height = keyHeight,
                fontSize = 8.5.sp,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.6f)
            ) { pressKey(KeycodeMap.KEY_BACKSPACE) }
        }

        // Row 2: QWERTY Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            KeyCap(
                label = "Tab",
                height = keyHeight,
                fontSize = 9.sp,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.3f)
            ) { pressKey(KeycodeMap.KEY_TAB) }

            for ((label, code) in QWERTY_ROW_1) {
                val displayLabel = if (capsActive || shiftActive) label.uppercase() else label
                KeyCap(label = displayLabel, height = keyHeight, fontSize = 10.5.sp, modifier = Modifier.weight(1f)) { pressKey(code) }
            }

            KeyCap(label = "[", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_LEFTBRACE) }
            KeyCap(label = "]", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_RIGHTBRACE) }
            KeyCap(label = "\\", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_BACKSLASH) }
        }

        // Row 3: Home Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            KeyCap(
                label = "Caps Lock",
                height = keyHeight,
                fontSize = 8.5.sp,
                active = capsActive,
                activeColor = accentColor,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.5f)
            ) {
                capsActive = !capsActive
                pressKey(KeycodeMap.KEY_CAPSLOCK)
            }

            for ((label, code) in QWERTY_ROW_2) {
                val displayLabel = if (capsActive || shiftActive) label.uppercase() else label
                KeyCap(label = displayLabel, height = keyHeight, fontSize = 10.5.sp, modifier = Modifier.weight(1f)) { pressKey(code) }
            }

            KeyCap(label = ";", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_SEMICOLON) }
            KeyCap(label = "'", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_APOSTROPHE) }
            KeyCap(
                label = "Enter",
                height = keyHeight,
                fontSize = 9.5.sp,
                customBg = accentColor,
                customFg = MonochromeWhite,
                modifier = Modifier.weight(2.1f)
            ) { pressKey(KeycodeMap.KEY_ENTER) }
        }

        // Row 4: Shift Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            KeyCap(
                label = "Shift",
                height = keyHeight,
                fontSize = 9.sp,
                active = shiftActive,
                activeColor = accentColor,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.8f)
            ) { shiftActive = !shiftActive }

            for ((label, code) in QWERTY_ROW_3) {
                val displayLabel = if (capsActive || shiftActive) label.uppercase() else label
                KeyCap(label = displayLabel, height = keyHeight, fontSize = 10.5.sp, modifier = Modifier.weight(1f)) { pressKey(code) }
            }

            KeyCap(label = ",", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_COMMA) }
            KeyCap(label = ".", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_DOT) }
            KeyCap(label = "/", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_SLASH) }

            KeyCap(
                label = "Shift",
                height = keyHeight,
                fontSize = 9.sp,
                active = shiftActive,
                activeColor = accentColor,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.5f)
            ) { shiftActive = !shiftActive }

            // Accented Arrow Up Key
            KeyCap(
                label = "↑",
                height = keyHeight,
                fontSize = 11.sp,
                customBg = accentColor,
                customFg = MonochromeWhite,
                modifier = Modifier.weight(1f)
            ) { pressKey(KeycodeMap.KEY_UP_ARROW) }
        }

        // Row 5: Physical Bottom Deck (Wide Spacebar + Accented Arrow Keys)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            KeyCap(
                label = "Ctrl",
                active = ctrlActive,
                height = keyHeight,
                fontSize = 9.sp,
                activeColor = accentColor,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.1f)
            ) { ctrlActive = !ctrlActive }

            KeyCap(
                label = "Win",
                active = winActive,
                height = keyHeight,
                fontSize = 9.sp,
                activeColor = accentColor,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.1f)
            ) { winActive = !winActive }

            KeyCap(
                label = "Alt",
                active = altActive,
                height = keyHeight,
                fontSize = 9.sp,
                activeColor = accentColor,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.1f)
            ) { altActive = !altActive }

            // WIDE LIGHT PHYSICAL SPACEBAR (weight 5.8f)
            KeyCap(
                label = "SPACE",
                height = keyHeight,
                fontSize = 10.sp,
                customBg = Color(0xFFD6D6D6),
                customFg = PitchBlack,
                modifier = Modifier.weight(5.8f)
            ) { pressKey(KeycodeMap.KEY_SPACE) }

            KeyCap(
                label = "Alt",
                active = altActive,
                height = keyHeight,
                fontSize = 9.sp,
                activeColor = accentColor,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.1f)
            ) { altActive = !altActive }

            KeyCap(
                label = "Fn",
                height = keyHeight,
                fontSize = 9.sp,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1f)
            ) {}

            KeyCap(
                label = "Ctrl",
                active = ctrlActive,
                height = keyHeight,
                fontSize = 9.sp,
                activeColor = accentColor,
                customBg = CharcoalDark,
                modifier = Modifier.weight(1.1f)
            ) { ctrlActive = !ctrlActive }

            // Accented Arrow Keys Cluster (← ↓ →)
            KeyCap(
                label = "←",
                height = keyHeight,
                fontSize = 11.sp,
                customBg = accentColor,
                customFg = MonochromeWhite,
                modifier = Modifier.weight(1f)
            ) { pressKey(KeycodeMap.KEY_LEFT_ARROW) }

            KeyCap(
                label = "↓",
                height = keyHeight,
                fontSize = 11.sp,
                customBg = accentColor,
                customFg = MonochromeWhite,
                modifier = Modifier.weight(1f)
            ) { pressKey(KeycodeMap.KEY_DOWN_ARROW) }

            KeyCap(
                label = "→",
                height = keyHeight,
                fontSize = 11.sp,
                customBg = accentColor,
                customFg = MonochromeWhite,
                modifier = Modifier.weight(1f)
            ) { pressKey(KeycodeMap.KEY_RIGHT_ARROW) }
        }
    }
}

@Composable
fun KeyCap(
    label: String,
    modifier: Modifier = Modifier,
    height: Dp = 38.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = 10.sp,
    active: Boolean = false,
    activeColor: Color = SignalRed,
    customBg: Color? = null,
    customFg: Color? = null,
    onClick: () -> Unit
) {
    val bgColor = when {
        active -> activeColor
        customBg != null -> customBg
        else -> Color(0xFF242426)
    }
    val textColor = when {
        active -> PitchBlack
        customFg != null -> customFg
        else -> MonochromeWhite
    }

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, if (active) activeColor else GraphiteBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = DotMatrixTypography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            ),
            color = textColor,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
