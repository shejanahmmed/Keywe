package com.shejan.keywe.ui.touchpad

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.keywe.bt.HidReportDescriptor
import com.shejan.keywe.ui.theme.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

private class TouchState {
    var lastX: Float = 0f
    var lastY: Float = 0f
    var downX: Float = 0f
    var downY: Float = 0f
    var touchDownTime: Long = 0L
    var isDragging: Boolean = false
    var peakPointerCount: Int = 0
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TouchpadSurface(
    onMouseInput: (buttons: Byte, dx: Byte, dy: Byte, wheel: Byte) -> Unit,
    modifier: Modifier = Modifier,
    sensitivity: Float = 1.2f,
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

    val state = remember { TouchState() }
    var touchIndicatorPos by remember { mutableStateOf(Offset.Unspecified) }

    // Unified Modern Trackpad Deck Module
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalDark)
            .border(1.dp, GraphiteBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        // Integrated Trackpad Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(PitchBlack)
                .border(1.dp, GraphiteSubtle, RoundedCornerShape(8.dp))
        ) {
            // Touchpad Active Field (Touch Surface)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInteropFilter { event ->
                        val pointerCount = event.pointerCount

                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                state.lastX = event.x
                                state.lastY = event.y
                                state.downX = event.x
                                state.downY = event.y
                                state.touchDownTime = System.currentTimeMillis()
                                state.isDragging = false
                                state.peakPointerCount = 1
                                touchIndicatorPos = Offset(event.x, event.y)
                            }

                            MotionEvent.ACTION_POINTER_DOWN -> {
                                state.lastX = event.x
                                state.lastY = event.y
                                if (pointerCount > state.peakPointerCount) {
                                    state.peakPointerCount = pointerCount
                                }
                                state.isDragging = false
                                state.downX = event.x
                                state.downY = event.y
                                state.touchDownTime = System.currentTimeMillis()
                                touchIndicatorPos = Offset(event.x, event.y)
                            }

                            MotionEvent.ACTION_MOVE -> {
                                val deltaX = event.x - state.lastX
                                val deltaY = event.y - state.lastY

                                val totalDx = event.x - state.downX
                                val totalDy = event.y - state.downY
                                val totalDisplacement = sqrt(totalDx * totalDx + totalDy * totalDy)
                                if (totalDisplacement > 8f) {
                                    state.isDragging = true
                                }

                                state.lastX = event.x
                                state.lastY = event.y
                                touchIndicatorPos = Offset(event.x, event.y)

                                if (state.isDragging) {
                                    val dxRaw = deltaX * sensitivity
                                    val dyRaw = deltaY * sensitivity

                                    if (pointerCount == 1) {
                                        val dxByte = dxRaw.coerceIn(-127f, 127f).roundToInt().toByte()
                                        val dyByte = dyRaw.coerceIn(-127f, 127f).roundToInt().toByte()
                                        if (dxByte != 0.toByte() || dyByte != 0.toByte()) {
                                            onMouseInput(0, dxByte, dyByte, 0)
                                        }
                                    } else if (pointerCount == 2) {
                                        val absDx = kotlin.math.abs(dxRaw)
                                        val absDy = kotlin.math.abs(dyRaw)
                                        if (absDy >= absDx) {
                                            val wheelByte = (-dyRaw).coerceIn(-127f, 127f).roundToInt().toByte()
                                            if (wheelByte != 0.toByte()) {
                                                onMouseInput(0, 0, 0, wheelByte)
                                            }
                                        } else {
                                            val panByte = dxRaw.coerceIn(-127f, 127f).roundToInt().toByte()
                                            if (panByte != 0.toByte()) {
                                                onMouseInput(0, panByte, 0, 0)
                                            }
                                        }
                                    }
                                }
                            }

                            MotionEvent.ACTION_POINTER_UP -> {
                                state.lastX = event.x
                                state.lastY = event.y
                            }

                            MotionEvent.ACTION_UP -> {
                                val duration = System.currentTimeMillis() - state.touchDownTime

                                if (!state.isDragging && duration < 350L) {
                                    triggerHapticPulse()
                                    when (state.peakPointerCount) {
                                        1 -> {
                                            onMouseInput(HidReportDescriptor.MOUSE_BUTTON_LEFT, 0, 0, 0)
                                            onMouseInput(0, 0, 0, 0)
                                        }
                                        2 -> {
                                            onMouseInput(HidReportDescriptor.MOUSE_BUTTON_RIGHT, 0, 0, 0)
                                            onMouseInput(0, 0, 0, 0)
                                        }
                                    }
                                }

                                state.isDragging = false
                                state.peakPointerCount = 0
                                touchIndicatorPos = Offset.Unspecified
                            }

                            MotionEvent.ACTION_CANCEL -> {
                                state.isDragging = false
                                state.peakPointerCount = 0
                                touchIndicatorPos = Offset.Unspecified
                            }
                        }
                        true
                    },
                contentAlignment = Alignment.Center
            ) {
                // Subtle Dot Grid background on touchpad field
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 24.dp.toPx()
                    val dotColor = MonochromeDarkText.copy(alpha = 0.35f)
                    val dotRadius = 1.dp.toPx()
                    for (x in 0..size.width.toInt() step step.toInt()) {
                        for (y in 0..size.height.toInt() step step.toInt()) {
                            drawCircle(
                                color = dotColor,
                                radius = dotRadius,
                                center = Offset(x.toFloat(), y.toFloat())
                            )
                        }
                    }

                    // Live touch indicator: glowing dot that follows the finger
                    if (touchIndicatorPos != Offset.Unspecified) {
                        // Outer glow ring
                        drawCircle(
                            color = MonochromeWhite.copy(alpha = 0.10f),
                            radius = 28.dp.toPx(),
                            center = touchIndicatorPos
                        )
                        // Mid glow ring
                        drawCircle(
                            color = MonochromeWhite.copy(alpha = 0.18f),
                            radius = 14.dp.toPx(),
                            center = touchIndicatorPos
                        )
                        // Solid core dot
                        drawCircle(
                            color = MonochromeWhite.copy(alpha = 0.75f),
                            radius = 4.dp.toPx(),
                            center = touchIndicatorPos
                        )
                    }
                }

                Text(
                    text = "TOUCHPAD :: MULTI-TOUCH SURFACE",
                    style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                    color = MonochromeDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Subtle Horizontal Divider Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(GraphiteSubtle)
            )

            // Integrated Bottom Click Zone (L-CLICK & R-CLICK built directly into the trackpad)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(CharcoalDark)
            ) {
                // Integrated Left Click Zone
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            triggerHapticPulse()
                            onMouseInput(HidReportDescriptor.MOUSE_BUTTON_LEFT, 0, 0, 0)
                            onMouseInput(0, 0, 0, 0)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[ L-CLICK ]",
                        style = DotMatrixTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MonochromeWhite,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Vertical Divider Line between Left & Right Click
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(GraphiteSubtle)
                )

                // Integrated Right Click Zone
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            triggerHapticPulse()
                            onMouseInput(HidReportDescriptor.MOUSE_BUTTON_RIGHT, 0, 0, 0)
                            onMouseInput(0, 0, 0, 0)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[ R-CLICK ]",
                        style = DotMatrixTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MonochromeWhite,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
