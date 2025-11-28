package com.biosense.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biosense.app.data.entity.ChatMessageEntity

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.isUser

    // Regex to find cards: [CARD:Title|Desc|Label]
    val cardRegex = "\\[CARD:(.*?)\\|(.*?)\\|(.*?)\\]".toRegex()

    // Split text into segments (Text parts vs Card parts)
    val parts = splitMessageWithCards(message.text, cardRegex)

    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isUser) 20.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 20.dp
    )

    val backgroundBrush = if (isUser) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1A4D5C).copy(alpha = 0.9f), Color(0xFF2D1B42).copy(alpha = 0.9f))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
        )
    }

    val borderBrush = Brush.verticalGradient(
        colors = listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.1f))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(bubbleShape)
                .border(width = 1.dp, brush = borderBrush, shape = bubbleShape)
                .background(brush = backgroundBrush)
                .padding(16.dp)
        ) {
            Column {
                parts.forEachIndexed { index, part ->
                    when (part) {
                        is MessagePart.Text -> {
                            if (part.content.isNotBlank()) {
                                // PARSE MARKDOWN HERE
                                val styledText = parseMarkdown(part.content)

                                Text(
                                    text = styledText,
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                        is MessagePart.Card -> {
                            if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                            ActionCard(
                                title = part.title,
                                description = part.description,
                                actionLabel = part.label,
                                onClick = { /* Handle action */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER CLASSES & FUNCTIONS ---

sealed class MessagePart {
    data class Text(val content: String) : MessagePart()
    data class Card(val title: String, val description: String, val label: String) : MessagePart()
}

/**
 * Splits the raw message string into regular text blocks and Action Card data classes.
 */
fun splitMessageWithCards(text: String, regex: Regex): List<MessagePart> {
    val parts = mutableListOf<MessagePart>()
    var currentIndex = 0

    regex.findAll(text).forEach { match ->
        if (match.range.first > currentIndex) {
            parts.add(MessagePart.Text(text.substring(currentIndex, match.range.first).trim()))
        }
        val (title, desc, label) = match.destructured
        parts.add(MessagePart.Card(title.trim(), desc.trim(), label.trim()))
        currentIndex = match.range.last + 1
    }

    if (currentIndex < text.length) {
        parts.add(MessagePart.Text(text.substring(currentIndex).trim()))
    }
    return parts
}