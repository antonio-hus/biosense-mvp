package com.biosense.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biosense.app.data.entity.ChatMessageEntity
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isUser = message.isUser

    // User messages: Rounded on Top-Left, Pointed on Bottom-Right
    // AI messages: Rounded on Top-Right, Pointed on Bottom-Left
    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    val timeString = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(message.timestamp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .glassEffect(shape = bubbleShape)
                    .padding(16.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = Color(0xFFE8E8ED),
                    lineHeight = 22.sp
                )
            }

            Text(
                text = timeString,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.padding(
                    top = 4.dp,
                    start = if (isUser) 0.dp else 8.dp,
                    end = if (isUser) 8.dp else 0.dp
                )
            )
        }
    }
}
