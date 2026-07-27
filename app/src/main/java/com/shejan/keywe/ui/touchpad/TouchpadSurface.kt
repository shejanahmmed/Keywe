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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.keywe.bt.HidReportDescriptor
import com.shejan.keywe.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TouchpadSurface(
    onMouseInput: (buttons: Byte, dx: Byte, dy: Byte, wheel: Byte) -> Unit,
    modifier: Modifier = Modifier,
    sensitivity: Float = 1.2f
) {
    var lastX by remember { mutableStateOf(0f) }
    var lastY by remember { mutableStateOf(0f) }
    var touchDownTime by remember { mutableStateOf(0L) }
    var activePointerCount by remember { mutableStateOf(0) }
    var isDragging by remember { mutableStateOf(false) }

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
                    activePointerCount = pointerCount

                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                            lastX = event.x
                            lastY = event.y
                            touchDownTime = System.currentTimeMillis()
                            isDragging = false
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val dxRaw = (event.x - lastX) * sensitivity
                            val dyRaw = (event.y - lastY) * sensitivity

                            if (kotlin.math.abs(dxRaw) > 1 || kotlin.math.abs(dyRaw) > 1) {
                                isDragging = true
                            }

                            lastX = event.x
                            lastY = event.y

                            if (pointerCount == 1) {
                                // Cursor Movement
                                val dxByte = dxRaw.coerceIn(-127f, 127f).roundToInt().toByte()
                                val dyByte = dyRaw.coerceIn(-127f, 127f).roundToInt().toByte()
                                onMouseInput(0, dxByte, dyByte, 0)
                            } else if (pointerCount == 2) {
                                // Two-Finger Vertical Scroll
                                val wheelByte = (-dyRaw).coerceIn(-127f, 127f).roundToInt().toByte()
                                onMouseInput(0, 0, 0, wheelByte)
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                            val duration = System.currentTimeMillis() - touchDownTime

                            // Tap Detection (< 200ms without dragging)
                            if (!isDragging && duration < 200) {
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
            // Subtle Dot Grid background on touchpad field
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 24.dp.toPx()
                for (x in 0..size.width.toInt() step step.toInt()) {
                    for (y in 0..size.height.toInt() step step.toInt()) {
                        drawCircle(
                            color = MonochromeDarkText.copy(alpha = 0.35f),
                            radius = 1.dp.toPx(),
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
