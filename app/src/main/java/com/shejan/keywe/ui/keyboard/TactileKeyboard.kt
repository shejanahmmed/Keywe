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

    const val KEY_RIGHT_ARROW: Byte = 0x4F
    const val KEY_LEFT_ARROW: Byte = 0x50
    const val KEY_DOWN_ARROW: Byte = 0x51
    const val KEY_UP_ARROW: Byte = 0x52
}

@Composable
fun TactileKeyboard(
    onSendKey: (modifiers: Byte, keycode: Byte) -> Unit,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 38.dp
) {
    var ctrlActive by remember { mutableStateOf(false) }
    var altActive by remember { mutableStateOf(false) }
    var shiftActive by remember { mutableStateOf(false) }
    var winActive by remember { mutableStateOf(false) }

    fun currentModifiers(): Byte {
        var mods = 0
        if (ctrlActive) mods = mods or HidReportDescriptor.MODIFIER_LEFT_CTRL.toInt()
        if (altActive) mods = mods or HidReportDescriptor.MODIFIER_LEFT_ALT.toInt()
        if (shiftActive) mods = mods or HidReportDescriptor.MODIFIER_LEFT_SHIFT.toInt()
        if (winActive) mods = mods or HidReportDescriptor.MODIFIER_LEFT_GUI.toInt()
        return mods.toByte()
    }

    fun pressKey(keycode: Byte) {
        val mods = currentModifiers()
        onSendKey(mods, keycode)
        onSendKey(mods, 0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalDark)
            .border(1.dp, GraphiteBorder, RoundedCornerShape(10.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Top Shortcut Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyCap(label = "WIN+D", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1f)) {
                onSendKey(HidReportDescriptor.MODIFIER_LEFT_GUI, KeycodeMap.KEY_D)
                onSendKey(0, 0)
            }
            KeyCap(label = "ALT+TAB", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1.1f)) {
                onSendKey(HidReportDescriptor.MODIFIER_LEFT_ALT, KeycodeMap.KEY_TAB)
                onSendKey(0, 0)
            }
            KeyCap(label = "WIN+L", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1f)) {
                onSendKey(HidReportDescriptor.MODIFIER_LEFT_GUI, KeycodeMap.KEY_L)
                onSendKey(0, 0)
            }
            KeyCap(label = "COPY", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1f)) {
                onSendKey(HidReportDescriptor.MODIFIER_LEFT_CTRL, KeycodeMap.KEY_C)
                onSendKey(0, 0)
            }
            KeyCap(label = "PASTE", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1f)) {
                onSendKey(HidReportDescriptor.MODIFIER_LEFT_CTRL, KeycodeMap.KEY_V)
                onSendKey(0, 0)
            }
        }

        // Modifiers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyCap(label = "CTRL", active = ctrlActive, height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { ctrlActive = !ctrlActive }
            KeyCap(label = "ALT", active = altActive, height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { altActive = !altActive }
            KeyCap(label = "WIN", active = winActive, height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { winActive = !winActive }
            KeyCap(label = "SHIFT", active = shiftActive, height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1f)) { shiftActive = !shiftActive }
            KeyCap(label = "ESC", height = keyHeight, fontSize = 10.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_ESCAPE) }
        }

        // Numbers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val numKeys = listOf(
                "1" to KeycodeMap.KEY_1, "2" to KeycodeMap.KEY_2, "3" to KeycodeMap.KEY_3,
                "4" to KeycodeMap.KEY_4, "5" to KeycodeMap.KEY_5, "6" to KeycodeMap.KEY_6,
                "7" to KeycodeMap.KEY_7, "8" to KeycodeMap.KEY_8, "9" to KeycodeMap.KEY_9,
                "0" to KeycodeMap.KEY_0
            )
            for ((label, code) in numKeys) {
                KeyCap(label = label, height = keyHeight, fontSize = 11.sp, modifier = Modifier.weight(1f)) { pressKey(code) }
            }
        }

        // QWERTY Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val row1 = listOf(
                "Q" to KeycodeMap.KEY_Q, "W" to KeycodeMap.KEY_W, "E" to KeycodeMap.KEY_E,
                "R" to KeycodeMap.KEY_R, "T" to KeycodeMap.KEY_T, "Y" to KeycodeMap.KEY_Y,
                "U" to KeycodeMap.KEY_U, "I" to KeycodeMap.KEY_I, "O" to KeycodeMap.KEY_O,
                "P" to KeycodeMap.KEY_P
            )
            for ((label, code) in row1) {
                KeyCap(label = label, height = keyHeight, fontSize = 11.sp, modifier = Modifier.weight(1f)) { pressKey(code) }
            }
        }

        // QWERTY Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val row2 = listOf(
                "A" to KeycodeMap.KEY_A, "S" to KeycodeMap.KEY_S, "D" to KeycodeMap.KEY_D,
                "F" to KeycodeMap.KEY_F, "G" to KeycodeMap.KEY_G, "H" to KeycodeMap.KEY_H,
                "J" to KeycodeMap.KEY_J, "K" to KeycodeMap.KEY_K, "L" to KeycodeMap.KEY_L
            )
            for ((label, code) in row2) {
                KeyCap(label = label, height = keyHeight, fontSize = 11.sp, modifier = Modifier.weight(1f)) { pressKey(code) }
            }
        }

        // QWERTY Row 3 + Backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val row3 = listOf(
                "Z" to KeycodeMap.KEY_Z, "X" to KeycodeMap.KEY_X, "C" to KeycodeMap.KEY_C,
                "V" to KeycodeMap.KEY_V, "B" to KeycodeMap.KEY_B, "N" to KeycodeMap.KEY_N,
                "M" to KeycodeMap.KEY_M
            )
            for ((label, code) in row3) {
                KeyCap(label = label, height = keyHeight, fontSize = 11.sp, modifier = Modifier.weight(1f)) { pressKey(code) }
            }
            KeyCap(label = "BKSP", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1.4f)) { pressKey(KeycodeMap.KEY_BACKSPACE) }
        }

        // Bottom Row: TAB, SPACE, ENTER, Arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            KeyCap(label = "TAB", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1f)) { pressKey(KeycodeMap.KEY_TAB) }
            KeyCap(label = "SPACE", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(2.5f)) { pressKey(KeycodeMap.KEY_SPACE) }
            KeyCap(label = "ENTER", height = keyHeight, fontSize = 9.sp, modifier = Modifier.weight(1.3f)) { pressKey(KeycodeMap.KEY_ENTER) }
            KeyCap(label = "▲", height = keyHeight, fontSize = 11.sp, modifier = Modifier.weight(0.7f)) { pressKey(KeycodeMap.KEY_UP_ARROW) }
            KeyCap(label = "▼", height = keyHeight, fontSize = 11.sp, modifier = Modifier.weight(0.7f)) { pressKey(KeycodeMap.KEY_DOWN_ARROW) }
            KeyCap(label = "◄", height = keyHeight, fontSize = 11.sp, modifier = Modifier.weight(0.7f)) { pressKey(KeycodeMap.KEY_LEFT_ARROW) }
            KeyCap(label = "►", height = keyHeight, fontSize = 11.sp, modifier = Modifier.weight(0.7f)) { pressKey(KeycodeMap.KEY_RIGHT_ARROW) }
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
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(4.dp))
            .background(if (active) activeColor else PitchBlack)
            .border(1.dp, if (active) activeColor else GraphiteBorder, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = DotMatrixTypography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            ),
            color = if (active) PitchBlack else MonochromeWhite,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
