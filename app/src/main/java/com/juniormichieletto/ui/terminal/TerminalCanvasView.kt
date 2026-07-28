package com.juniormichieletto.ui.terminal

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juniormichieletto.terminal.AnsiParser
import com.juniormichieletto.terminal.TerminalTab
import com.juniormichieletto.ui.theme.TerminalBackground
import com.juniormichieletto.ui.theme.TerminalBorder
import com.juniormichieletto.ui.theme.TerminalPrimary
import com.juniormichieletto.ui.theme.TerminalSurface
import com.juniormichieletto.ui.theme.TerminalTextSecondary

@Composable
fun TerminalCanvasView(
    activeTab: TerminalTab,
    fontSizeSp: Int,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit,
    onClearTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new output line
    LaunchedEffect(activeTab.lines.size) {
        if (activeTab.lines.isNotEmpty()) {
            listState.animateScrollToItem(activeTab.lines.size - 1)
        }
    }

    // Cursor blink animation
    val cursorTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by cursorTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TerminalBackground)
    ) {
        // Top status overlay bar with font zoom & clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalSurface)
                .border(width = 1.dp, color = TerminalBorder)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (activeTab.isConnected) TerminalPrimary else Color.Red,
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = activeTab.statusMessage,
                color = TerminalTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDecreaseFont,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = TerminalTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "${fontSizeSp}sp",
                color = TerminalTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            IconButton(
                onClick = onIncreaseFont,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = TerminalTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onClearTerminal,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear Output",
                    tint = TerminalTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        val uriHandler = LocalUriHandler.current

        // Terminal Output Screen
        SelectionContainer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("terminal_output_list")
            ) {
                items(
                    items = activeTab.lines,
                    key = { it.id }
                ) { line ->
                    val annotatedString = AnsiParser.parseAnsiToAnnotatedString(line.rawText)
                    ClickableText(
                        text = annotatedString,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp + 4).sp
                        ),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    try {
                                        uriHandler.openUri(annotation.item)
                                    } catch (_: Exception) {
                                    }
                                }
                        }
                    )
                }

                // Blinking block cursor row at bottom
                item(key = "cursor_row") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(width = (fontSizeSp * 0.6).dp, height = (fontSizeSp * 1.1).dp)
                                .alpha(cursorAlpha)
                                .background(TerminalPrimary, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }
        }
    }
}
