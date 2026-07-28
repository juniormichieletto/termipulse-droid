package com.juniormichieletto.ui.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juniormichieletto.terminal.TerminalTab
import com.juniormichieletto.ui.theme.TerminalBorder
import com.juniormichieletto.ui.theme.TerminalPrimary
import com.juniormichieletto.ui.theme.TerminalSurface
import com.juniormichieletto.ui.theme.TerminalSurfaceVariant
import com.juniormichieletto.ui.theme.TerminalWarning

@Composable
fun TerminalTabBar(
    tabs: List<TerminalTab>,
    activeTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onTabClosed: (Int) -> Unit,
    onAddTabClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .border(width = 1.dp, color = TerminalBorder)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isActive = index == activeTabIndex
                TerminalTabItem(
                    tab = tab,
                    isActive = isActive,
                    onClick = { onTabSelected(index) },
                    onClose = { onTabClosed(index) }
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onAddTabClick,
            modifier = Modifier
                .size(36.dp)
                .background(TerminalSurfaceVariant, CircleShape)
                .testTag("add_tab_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Tab",
                tint = TerminalPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TerminalTabItem(
    tab: TerminalTab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    val badgeColor = try {
        Color(android.graphics.Color.parseColor(tab.badgeColorHex))
    } catch (_: Exception) {
        TerminalPrimary
    }

    val pulseTransition = rememberInfiniteTransition(label = "job_pulse")
    val alphaAnim by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val backgroundColor = if (isActive) TerminalSurfaceVariant else TerminalSurface
    val borderColor = if (isActive) badgeColor else TerminalBorder

    Row(
        modifier = Modifier
            .testTag("tab_item_${tab.id}")
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Connection status indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (tab.isConnected) badgeColor else Color.Gray,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Long job indicator pulse icon
        AnimatedVisibility(visible = tab.isLongJobRunning) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Long Job Active",
                tint = TerminalWarning,
                modifier = Modifier
                    .size(14.dp)
                    .alpha(if (tab.isLongJobRunning) alphaAnim else 1.0f)
                    .padding(end = 2.dp)
            )
        }

        Text(
            text = tab.title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.clickable(onClick = onClick)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onClose,
            modifier = Modifier.size(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Tab",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
