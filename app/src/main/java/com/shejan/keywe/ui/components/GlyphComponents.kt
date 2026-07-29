package com.shejan.keywe.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.keywe.ui.theme.*

/**
 * Custom 2D Canvas Screen Rotation Icon graphic.
 */
@Composable
fun ScreenRotationGraphic(
    modifier: Modifier = Modifier,
    tint: Color = MonochromeWhite
) {
    Canvas(modifier = modifier.size(15.dp)) {
        val strokePx = 1.6.dp.toPx()
        val radius = size.minDimension * 0.40f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawArc(
            color = tint,
            startAngle = -30f,
            sweepAngle = 260f,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )

        drawRoundRect(
            color = tint,
            topLeft = Offset(center.x - radius * 0.45f, center.y - radius * 0.65f),
            size = Size(radius * 0.9f, radius * 1.3f),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = 1.2.dp.toPx())
        )
    }
}

/**
 * Custom 2D Canvas Settings Gear Icon graphic.
 */
@Composable
fun SettingsGearGraphic(
    modifier: Modifier = Modifier,
    tint: Color = MonochromeWhite
) {
    Canvas(modifier = modifier.size(15.dp)) {
        val radius = size.minDimension * 0.42f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            color = tint,
            radius = radius,
            center = center,
            style = Stroke(width = 1.6.dp.toPx())
        )
        drawCircle(
            color = tint,
            radius = radius * 0.4f,
            center = center
        )
    }
}

/**
 * LED Status dot indicator.
 */
@Composable
fun StatusIndicatorDot(
    color: Color = SignalRed,
    size: Dp = 8.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Minimal Dot Matrix Header with LED status dot, top-right Settings button, and crisp monospace typography.
 */
@Composable
fun DotMatrixHeader(
    title: String,
    subtitle: String = "",
    statusColor: Color = SignalRed,
    statusText: String = "STANDBY",
    onStatusClick: (() -> Unit)? = null,
    onToggleRotate: (() -> Unit)? = null,
    onOpenSettings: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
                text = title.uppercase(),
                style = DotMatrixTypography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle.uppercase(),
                    style = DotMatrixTypography.labelSmall.copy(fontSize = 10.sp),
                    color = MonochromeMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Status Badge Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CharcoalDark)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(4.dp))
                    .clickable(enabled = onStatusClick != null) { onStatusClick?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                StatusIndicatorDot(color = statusColor, size = 7.dp)
                Text(
                    text = statusText.uppercase(),
                    style = DotMatrixTypography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MonochromeWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Rotate Orientation Button Icon
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CharcoalDark)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(4.dp))
                    .clickable(enabled = onToggleRotate != null) { onToggleRotate?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                ScreenRotationGraphic()
            }

            // Settings Button Icon
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(CharcoalDark)
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(4.dp))
                    .clickable(enabled = onOpenSettings != null) { onOpenSettings?.invoke() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                SettingsGearGraphic()
            }
        }
    }
}

/**
 * Tactile Card container with crisp borders and subtle dark background.
 */
@Composable
fun TactileCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GraphiteBorder,
    backgroundColor: Color = CharcoalDark,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        content = content
    )
}

/**
 * Tactile Button component with clean single-line monospace label and long-press support.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TactileButton(
    text: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconGraphic: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    active: Boolean = false,
    accentColor: Color = SignalRed
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) accentColor else CharcoalDark)
            .border(
                width = 1.dp,
                color = if (active) accentColor else GraphiteBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (iconGraphic != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                iconGraphic()
                if (text.isNotEmpty()) {
                    Text(
                        text = text.uppercase(),
                        style = DotMatrixTypography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (active) PitchBlack else MonochromeWhite,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Text(
                text = text.uppercase(),
                style = DotMatrixTypography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = if (active) PitchBlack else MonochromeWhite,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
