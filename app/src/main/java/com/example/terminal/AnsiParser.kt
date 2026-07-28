package com.example.terminal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

data class StyledSegment(
    val text: String,
    val color: Color = Color(0xFFECEFF4),
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
)

object AnsiParser {
    private val DEFAULT_TEXT_COLOR = Color(0xFFECEFF4)
    private val ANSI_REGEX = Regex("\u001B\\[[0-9;]*[a-zA-Z]")

    fun parseAnsiToAnnotatedString(input: String): AnnotatedString {
        return buildAnnotatedString {
            var currentPos = 0
            var currentColor = DEFAULT_TEXT_COLOR
            var isBold = false
            var isItalic = false
            var isUnderline = false

            val matches = ANSI_REGEX.findAll(input)

            for (match in matches) {
                val start = match.range.first
                val end = match.range.last + 1

                if (start > currentPos) {
                    val textSegment = input.substring(currentPos, start)
                    appendSegment(textSegment, currentColor, isBold, isItalic, isUnderline)
                }

                val code = match.value
                val codes = code.removePrefix("\u001B[").dropLast(1).split(";").mapNotNull { it.toIntOrNull() }

                if (codes.isEmpty() || codes.contains(0)) {
                    currentColor = DEFAULT_TEXT_COLOR
                    isBold = false
                    isItalic = false
                    isUnderline = false
                }

                for (c in codes) {
                    when (c) {
                        0 -> {
                            currentColor = DEFAULT_TEXT_COLOR
                            isBold = false
                            isItalic = false
                            isUnderline = false
                        }
                        1 -> isBold = true
                        3 -> isItalic = true
                        4 -> isUnderline = true
                        30 -> currentColor = Color(0xFF2E3440) // Black
                        31 -> currentColor = Color(0xFFFF5252) // Red
                        32 -> currentColor = Color(0xFF00E676) // Green
                        33 -> currentColor = Color(0xFFFFC107) // Yellow
                        34 -> currentColor = Color(0xFF448AFF) // Blue
                        35 -> currentColor = Color(0xFFE040FB) // Magenta
                        36 -> currentColor = Color(0xFF00E5FF) // Cyan
                        37 -> currentColor = Color(0xFFECEFF4) // White
                        90 -> currentColor = Color(0xFF78909C) // Bright Black
                        91 -> currentColor = Color(0xFFFF8A80) // Bright Red
                        92 -> currentColor = Color(0xFFB9F6CA) // Bright Green
                        93 -> currentColor = Color(0xFFFFD180) // Bright Yellow
                        94 -> currentColor = Color(0xFF82B1FF) // Bright Blue
                        95 -> currentColor = Color(0xFFE1BEE7) // Bright Magenta
                        96 -> currentColor = Color(0xFF80D8FF) // Bright Cyan
                        97 -> currentColor = Color(0xFFFFFFFF) // Bright White
                    }
                }

                currentPos = end
            }

            if (currentPos < input.length) {
                val remaining = input.substring(currentPos)
                appendSegment(remaining, currentColor, isBold, isItalic, isUnderline)
            }
        }
    }

    private fun AnnotatedString.Builder.appendSegment(
        text: String,
        color: Color,
        isBold: Boolean,
        isItalic: Boolean,
        isUnderline: Boolean
    ) {
        val style = SpanStyle(
            color = color,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None
        )
        pushStyle(style)
        append(text)
        pop()
    }
}
