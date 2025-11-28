package com.biosense.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextIndent

/**
 * Enhanced Markdown parser for Jetpack Compose.
 * Supports: Headers (###), Bullet Lists (-), Bold (**), Italic (*).
 */
@Composable
fun parseMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")

        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()

            when {
                // HEADER (### Title)
                trimmed.startsWith("### ") -> {
                    val content = trimmed.removePrefix("### ").trim()
                    withStyle(
                        style = SpanStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF80CBC4) // Teal accent for headers
                        )
                    ) {
                        append(processInlineStyles(content))
                    }
                }

                // HEADER (## Title)
                trimmed.startsWith("## ") -> {
                    val content = trimmed.removePrefix("## ").trim()
                    withStyle(
                        style = SpanStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF4DB6AC)
                        )
                    ) {
                        append(processInlineStyles(content))
                    }
                }

                // BULLET LIST (- Item or * Item)
                (trimmed.startsWith("- ") || trimmed.startsWith("* ")) -> {
                    val content = trimmed.substring(2).trim()

                    // Add a bullet point with indent
                    withStyle(ParagraphStyle(textIndent = TextIndent(firstLine = 0.sp, restLine = 12.sp))) {
                        append("• ")
                        append(processInlineStyles(content))
                    }
                }

                // NORMAL TEXT
                else -> {
                    append(processInlineStyles(trimmed))
                }
            }

            // Add newline if it's not the last line
            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

/**
 * Helper to handle inline bold (**text**) and italic (*text*) parsing.
 */
fun processInlineStyles(text: String): AnnotatedString {
    // Regex matches: **Bold** OR *Italic*
    // Group 2 = Bold content, Group 4 = Italic content
    val regex = "(\\*\\*(.*?)\\*\\*)|(\\*([^*]+)\\*)".toRegex()

    return buildAnnotatedString {
        var currentIndex = 0

        regex.findAll(text).forEach { match ->
            // Append plain text before match
            if (match.range.first > currentIndex) {
                append(text.substring(currentIndex, match.range.first))
            }

            val boldContent = match.groups[2]?.value
            val italicContent = match.groups[4]?.value

            if (boldContent != null) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFFB2DFDB))) {
                    append(boldContent)
                }
            } else if (italicContent != null) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(italicContent)
                }
            }

            currentIndex = match.range.last + 1
        }

        // Append remaining text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
