package com.shejan.keywe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.keywe.ui.theme.*

/**
 * LED Status dot indicator.
 */
@Composable
fun StatusIndicatorDot(
    color: Color = SignalRed,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Minimal Dot Matrix Header with LED status dot and crisp monospace typography.
 */
@Composable
fun DotMatrixHeader(
    title: String,
    subtitle: String = "",
    statusColor: Color = SignalRed,
    statusText: String = "STANDBY",
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

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(CharcoalDark)
                .border(1.dp, GraphiteBorder, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 5.dp)
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
 * Tactile Button component with clean single-line monospace label.
 */
@Composable
fun TactileButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
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
