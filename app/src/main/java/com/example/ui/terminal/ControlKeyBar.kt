package com.example.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TerminalBorder
import com.example.ui.theme.TerminalPrimary
import com.example.ui.theme.TerminalSecondary
import com.example.ui.theme.TerminalSurface
import com.example.ui.theme.TerminalSurfaceVariant
import com.example.ui.theme.TerminalTextPrimary

@Composable
fun ControlKeyBar(
    isCtrlActive: Boolean,
    isAltActive: Boolean,
    onToggleCtrl: () -> Unit,
    onToggleAlt: () -> Unit,
    onKeyClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val keyList = listOf(
        "Esc", "Tab", "Ctrl+C", "Ctrl+L", "Ctrl+Z", "Ctrl+D",
        "▲", "▼", "◄", "►",
        "|", "/", "\\", "~", "-", "_", "$", ";", ":", "=", "`"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TerminalSurface)
            .border(width = 1.dp, color = TerminalBorder)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sticky Ctrl Toggle
        KeyButton(
            text = "Ctrl",
            isActive = isCtrlActive,
            onClick = onToggleCtrl,
            activeColor = TerminalPrimary,
            tag = "btn_ctrl_toggle"
        )

        // Sticky Alt Toggle
        KeyButton(
            text = "Alt",
            isActive = isAltActive,
            onClick = onToggleAlt,
            activeColor = TerminalSecondary,
            tag = "btn_alt_toggle"
        )

        // General Keys
        keyList.forEach { key ->
            KeyButton(
                text = key,
                isActive = false,
                onClick = { onKeyClick(key) },
                tag = "btn_key_$key"
            )
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    activeColor: androidx.compose.ui.graphics.Color = TerminalPrimary,
    tag: String = ""
) {
    val bgColor = if (isActive) activeColor.copy(alpha = 0.25f) else TerminalSurfaceVariant
    val borderColor = if (isActive) activeColor else TerminalBorder
    val textColor = if (isActive) activeColor else TerminalTextPrimary

    Box(
        modifier = Modifier
            .height(38.dp)
            .widthIn(min = 40.dp)
            .background(bgColor, RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
