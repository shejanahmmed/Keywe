package com.shejan.keywe.ui.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.keywe.bt.HidReportDescriptor
import com.shejan.keywe.ui.components.StatusIndicatorDot
import com.shejan.keywe.ui.components.TactileButton
import com.shejan.keywe.ui.theme.*

object KeycodeConverter {
    /**
     * Converts a single character to USB HID Modifier and Keycode.
     */
    fun charToHidKey(char: Char): Pair<Byte, Byte>? {
        return when (char) {
            in 'a'..'z' -> 0.toByte() to (0x04 + (char - 'a')).toByte()
            in 'A'..'Z' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to (0x04 + (char - 'A')).toByte()
            in '1'..'9' -> 0.toByte() to (0x1E + (char - '1')).toByte()
            '0' -> 0.toByte() to 0x27.toByte()
            '!' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x1E.toByte()
            '@' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x1F.toByte()
            '#' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x20.toByte()
            '$' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x21.toByte()
            '%' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x22.toByte()
            '^' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x23.toByte()
            '&' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x24.toByte()
            '*' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x25.toByte()
            '(' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x26.toByte()
            ')' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x27.toByte()
            ' ' -> 0.toByte() to 0x2C.toByte()
            '\n' -> 0.toByte() to 0x28.toByte()
            '\t' -> 0.toByte() to 0x2B.toByte()
            '-' -> 0.toByte() to 0x2D.toByte()
            '_' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x2D.toByte()
            '=' -> 0.toByte() to 0x2E.toByte()
            '+' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x2E.toByte()
            '[' -> 0.toByte() to 0x2F.toByte()
            '{' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x2F.toByte()
            ']' -> 0.toByte() to 0x30.toByte()
            '}' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x30.toByte()
            '\\' -> 0.toByte() to 0x31.toByte()
            '|' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x31.toByte()
            ';' -> 0.toByte() to 0x33.toByte()
            ':' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x33.toByte()
            '\'' -> 0.toByte() to 0x34.toByte()
            '"' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x34.toByte()
            '`' -> 0.toByte() to 0x35.toByte()
            '~' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x35.toByte()
            ',' -> 0.toByte() to 0x36.toByte()
            '<' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x36.toByte()
            '.' -> 0.toByte() to 0x37.toByte()
            '>' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x37.toByte()
            '/' -> 0.toByte() to 0x38.toByte()
            '?' -> HidReportDescriptor.MODIFIER_LEFT_SHIFT to 0x38.toByte()
            else -> null
        }
    }
}

/**
 * Surface composable capturing Gboard / System Soft Keyboard inputs
 * and streaming HID keystrokes to PC host.
 */
@Composable
fun SystemKeyboardSurface(
    onSendKey: (modifiers: Byte, keycode: Byte) -> Unit,
    modifier: Modifier = Modifier
) {
    var tfValue by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val clipboardManager = remember(context) {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

    fun sendKeyPress(modifiers: Byte, keycode: Byte) {
        onSendKey(modifiers, keycode)
        onSendKey(0, 0) // Release key
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalDark)
            .border(1.dp, GraphiteBorder, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Status Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusIndicatorDot(color = MatrixGreen)
                Text(
                    text = "SYSTEM KEYBOARD (PHONE IME)",
                    style = DotMatrixTypography.labelSmall.copy(fontSize = 11.sp),
                    color = MatrixGreen
                )
            }
            Text(
                text = "[ TAP TO FOCUS ]",
                style = DotMatrixTypography.labelSmall.copy(fontSize = 9.5.sp),
                color = MonochromeMuted,
                modifier = Modifier.clickable {
                    try { focusRequester.requestFocus() } catch (_: Exception) {}
                }
            )
        }

        // Live Text Terminal Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(PitchBlack)
                .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp))
                .clickable {
                    try { focusRequester.requestFocus() } catch (_: Exception) {}
                }
                .padding(8.dp)
        ) {
            TextField(
                value = tfValue,
                onValueChange = { newValue ->
                    val oldText = tfValue.text
                    val newText = newValue.text

                    if (newText.length > oldText.length) {
                        val addedChars = newText.substring(oldText.length)
                        for (ch in addedChars) {
                            val hidKey = KeycodeConverter.charToHidKey(ch)
                            if (hidKey != null) {
                                sendKeyPress(hidKey.first, hidKey.second)
                            }
                        }
                    } else if (newText.length < oldText.length) {
                        val diffCount = oldText.length - newText.length
                        repeat(diffCount) {
                            sendKeyPress(0, 0x2A.toByte()) // Backspace
                        }
                    }
                    tfValue = newValue
                },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                textStyle = DotMatrixTypography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = MonochromeWhite
                ),
                placeholder = {
                    Text(
                        text = "Type here using your phone's keyboard...",
                        style = DotMatrixTypography.bodyMedium.copy(fontSize = 12.sp),
                        color = MonochromeMuted
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        sendKeyPress(0, 0x28.toByte()) // Enter key
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = SignalRed
                )
            )
        }

        // Quick Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TactileButton(
                text = "BKSP",
                onClick = { sendKeyPress(0, 0x2A.toByte()) },
                modifier = Modifier.weight(1f)
            )
            TactileButton(
                text = "SPACE",
                onClick = { sendKeyPress(0, 0x2C.toByte()) },
                modifier = Modifier.weight(1.2f)
            )
            TactileButton(
                text = "ENTER",
                onClick = {
                    val updated = tfValue.text + "\n"
                    tfValue = TextFieldValue(text = updated, selection = TextRange(updated.length))
                    sendKeyPress(0, 0x28.toByte())
                },
                modifier = Modifier.weight(1f)
            )
            TactileButton(
                text = "CLEAR",
                onClick = {
                    // Send Ctrl+A (select all) then Delete to the PC so the remote
                    // input field is wiped in sync with the local text box clear.
                    sendKeyPress(HidReportDescriptor.MODIFIER_LEFT_CTRL, 0x04.toByte()) // Ctrl+A
                    sendKeyPress(0, 0x2A.toByte()) // Backspace / Delete selection
                    // Clear the local text box
                    tfValue = TextFieldValue("", selection = TextRange(0))
                },
                modifier = Modifier.weight(1f),
                accentColor = SignalRed
            )
        }

        // Scrollable Emoji Strip & Dedicated Line Break Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scrollable Emoji Bar
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PitchBlack)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val emojis = listOf("😊", "😂", "❤️", "👍", "🔥", "🎉", "🚀", "🙏", "👏", "😎", "💯", "✨", "💡", "✅", "😃", "🤩", "😍", "🥳")
                emojis.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(CharcoalDark)
                            .border(1.dp, GraphiteBorder, RoundedCornerShape(4.dp))
                            .clickable {
                                // Append emoji to the local text box only.
                                // Standard Bluetooth HID has no Unicode transport — emojis cannot
                                // be transmitted as HID keycodes and are silently dropped if attempted.
                                // The user should type text with emojis here and use "PASTE TO PC"
                                // if their PC OS supports Unicode paste (Windows: Win+. emoji picker
                                // is a better alternative for actual PC emoji input).
                                val updated = tfValue.text + emoji
                                tfValue = TextFieldValue(text = updated, selection = TextRange(updated.length))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Right Side Dedicated Line Break Button
            TactileButton(
                text = "LINE BREAK",
                onClick = {
                    val updated = tfValue.text + "\n"
                    tfValue = TextFieldValue(text = updated, selection = TextRange(updated.length))
                    sendKeyPress(0, 0x28.toByte())
                },
                modifier = Modifier.width(105.dp),
                accentColor = SignalRed
            )
        }

        // Emoji notice: standard Bluetooth HID cannot transmit Unicode/emoji codepoints.
        // Emojis tapped above are added to the local text box only.
        Text(
            text = "⚠ Emojis are stored in the text box only — Bluetooth HID cannot transmit Unicode to PC",
            style = DotMatrixTypography.labelSmall.copy(fontSize = 8.5.sp),
            color = MonochromeMuted,
            modifier = Modifier.fillMaxWidth()
        )


        // Clipboard Copy & Paste to PC Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TactileButton(
                text = "COPY (CTRL+C)",
                onClick = {
                    sendKeyPress(HidReportDescriptor.MODIFIER_LEFT_CTRL, 0x06.toByte())
                    if (tfValue.text.isNotEmpty()) {
                        val clip = ClipData.newPlainText("Keywe Text", tfValue.text)
                        clipboardManager?.setPrimaryClip(clip)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            TactileButton(
                text = "PASTE TO PC",
                onClick = {
                    val clipData = clipboardManager?.primaryClip
                    val clipText = if (clipData != null && clipData.itemCount > 0) {
                        clipData.getItemAt(0).text?.toString()
                    } else null

                    if (!clipText.isNullOrEmpty()) {
                        val updated = tfValue.text + clipText
                        tfValue = TextFieldValue(text = updated, selection = TextRange(updated.length))
                        for (ch in clipText) {
                            val hidKey = KeycodeConverter.charToHidKey(ch)
                            if (hidKey != null) {
                                sendKeyPress(hidKey.first, hidKey.second)
                            }
                        }
                    } else {
                        // Fallback: Send Ctrl+V directly to PC
                        sendKeyPress(HidReportDescriptor.MODIFIER_LEFT_CTRL, 0x19.toByte())
                    }
                },
                modifier = Modifier.weight(1f),
                accentColor = MatrixGreen
            )
        }
    }
}
