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

private class TouchState {
    var lastX: Float = 0f
    var lastY: Float = 0f
    var touchDownTime: Long = 0L
    var isDragging: Boolean = false
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TouchpadSurface(
    onMouseInput: (buttons: Byte, dx: Byte, dy: Byte, wheel: Byte) -> Unit,
    modifier: Modifier = Modifier,
    sensitivity: Float = 1.2f
) {
    val state = remember { TouchState() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CharcoalDark)
            .border(1.dp, GraphiteBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        // Touchpad Active Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(PitchBlack)
                .border(1.dp, GraphiteSubtle, RoundedCornerShape(6.dp))
                .pointerInteropFilter { event ->
                    val pointerCount = event.pointerCount

                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                            state.lastX = event.x
                            state.lastY = event.y
                            state.touchDownTime = System.currentTimeMillis()
                            state.isDragging = false
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val dxRaw = (event.x - state.lastX) * sensitivity
                            val dyRaw = (event.y - state.lastY) * sensitivity

                            if (kotlin.math.abs(dxRaw) > 1 || kotlin.math.abs(dyRaw) > 1) {
                                state.isDragging = true
                            }

                            state.lastX = event.x
                            state.lastY = event.y

                            if (pointerCount == 1) {
                                // Cursor Movement
                                val dxByte = dxRaw.coerceIn(-127f, 127f).roundToInt().toByte()
                                val dyByte = dyRaw.coerceIn(-127f, 127f).roundToInt().toByte()
                                if (dxByte != 0.toByte() || dyByte != 0.toByte()) {
                                    onMouseInput(0, dxByte, dyByte, 0)
                                }
                            } else if (pointerCount == 2) {
                                // Two-Finger Vertical Scroll
                                val wheelByte = (-dyRaw).coerceIn(-127f, 127f).roundToInt().toByte()
                                if (wheelByte != 0.toByte()) {
                                    onMouseInput(0, 0, 0, wheelByte)
                                }
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                            val duration = System.currentTimeMillis() - state.touchDownTime

                            // Tap Detection (< 200ms without dragging)
                            if (!state.isDragging && duration < 200) {
                                if (pointerCount == 1) {
                                    // Single Finger Tap -> Left Click
                                    onMouseInput(HidReportDescriptor.MOUSE_BUTTON_LEFT, 0, 0, 0)
                                    onMouseInput(0, 0, 0, 0) // Release
                                } else if (pointerCount == 2) {
                                    // Two Finger Tap -> Right Click
                                    onMouseInput(HidReportDescriptor.MOUSE_BUTTON_RIGHT, 0, 0, 0)
                                    onMouseInput(0, 0, 0, 0) // Release
                                }
                            }
                        }
                    }
                    true
                },
            contentAlignment = Alignment.Center
        ) {
            // Subtle Dot Grid background on touchpad field (renders once, zero recomposition overhead)
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
            }

            Text(
                text = "TOUCHPAD :: MULTI-TOUCH SURFACE",
                style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                color = MonochromeDarkText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hardware Left & Right Click Tactile Pads
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left Click Pad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(GraphiteSubtle)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp))
                    .clickable {
                        onMouseInput(HidReportDescriptor.MOUSE_BUTTON_LEFT, 0, 0, 0)
                        onMouseInput(0, 0, 0, 0)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ L-CLICK ]",
                    style = DotMatrixTypography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MonochromeWhite,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Right Click Pad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(GraphiteSubtle)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(6.dp))
                    .clickable {
                        onMouseInput(HidReportDescriptor.MOUSE_BUTTON_RIGHT, 0, 0, 0)
                        onMouseInput(0, 0, 0, 0)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ R-CLICK ]",
                    style = DotMatrixTypography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = MonochromeWhite,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
