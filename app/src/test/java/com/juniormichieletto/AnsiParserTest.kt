package com.juniormichieletto

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.juniormichieletto.terminal.AnsiParser
import com.juniormichieletto.terminal.StyledSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnsiParserTest {

    @Test
    fun testStyledSegmentDefaults() {
        val segment = StyledSegment("test")
        assertEquals("test", segment.text)
        assertEquals(Color(0xFFECEFF4), segment.color)
        assertFalse(segment.isBold)
        assertFalse(segment.isItalic)
        assertFalse(segment.isUnderline)
    }

    @Test
    fun testStyledSegmentCustom() {
        val segment = StyledSegment(
            text = "custom",
            color = Color.Red,
            isBold = true,
            isItalic = true,
            isUnderline = true
        )
        assertEquals("custom", segment.text)
        assertEquals(Color.Red, segment.color)
        assertTrue(segment.isBold)
        assertTrue(segment.isItalic)
        assertTrue(segment.isUnderline)
    }

    @Test
    fun testParsePlainTextWithoutAnsi() {
        val text = "Hello World"
        val result = AnsiParser.parseAnsiToAnnotatedString(text)
        assertEquals("Hello World", result.text)
    }

    @Test
    fun testParseEmptyString() {
        val result = AnsiParser.parseAnsiToAnnotatedString("")
        assertEquals("", result.text)
    }

    @Test
    fun testParseColorCodes() {
        // Red color code: \u001B[31m
        val input = "\u001B[31mRed Text\u001B[0m"
        val result = AnsiParser.parseAnsiToAnnotatedString(input)
        assertEquals("Red Text", result.text)
        assertEquals(1, result.spanStyles.size)
        assertEquals(Color(0xFFFF5252), result.spanStyles[0].item.color)
    }

    @Test
    fun testParseFormattingCodes() {
        // Bold: \u001B[1m, Italic: \u001B[3m, Underline: \u001B[4m
        val input = "\u001B[1mBold\u001B[0m \u001B[4mUnderline\u001B[0m"
        val result = AnsiParser.parseAnsiToAnnotatedString(input)
        assertEquals("Bold Underline", result.text)
        assertTrue(result.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(result.spanStyles.any { it.item.textDecoration == TextDecoration.Underline })
    }

    @Test
    fun testParseBrightColors() {
        val input = "\u001B[90mBright Black\u001B[91mBright Red\u001B[92mBright Green\u001B[93mBright Yellow\u001B[94mBright Blue\u001B[95mBright Magenta\u001B[96mBright Cyan\u001B[97mBright White\u001B[0m"
        val result = AnsiParser.parseAnsiToAnnotatedString(input)
        assertEquals("Bright BlackBright RedBright GreenBright YellowBright BlueBright MagentaBright CyanBright White", result.text)
        assertTrue(result.spanStyles.isNotEmpty())
    }

    @Test
    fun testParseAllStandardForegroundColors() {
        val input = "\u001B[30mBlack\u001B[32mGreen\u001B[33mYellow\u001B[34mBlue\u001B[35mMagenta\u001B[36mCyan\u001B[37mWhite\u001B[0m"
        val result = AnsiParser.parseAnsiToAnnotatedString(input)
        assertEquals("BlackGreenYellowBlueMagentaCyanWhite", result.text)
        assertEquals(7, result.spanStyles.size)
    }

    @Test
    fun testParseCompoundAndUnknownCodes() {
        val input = "\u001B[1;31mBold Red\u001B[99mUnknown Code Text\u001B[mReset"
        val result = AnsiParser.parseAnsiToAnnotatedString(input)
        assertEquals("Bold RedUnknown Code TextReset", result.text)
    }

    @Test
    fun testParseUrlAnnotations() {
        val input = "Check https://github.com/juniormichieletto for details."
        val result = AnsiParser.parseAnsiToAnnotatedString(input)
        assertEquals("Check https://github.com/juniormichieletto for details.", result.text)

        val annotations = result.getStringAnnotations(tag = "URL", start = 0, end = result.text.length)
        assertEquals(1, annotations.size)
        assertEquals("https://github.com/juniormichieletto", annotations[0].item)
    }
}
