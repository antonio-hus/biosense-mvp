package com.biosense.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun ChatInputField(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Text Input Area
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .glassEffect(shape = RoundedCornerShape(28.dp))
        ) {
            TextField(
                value = messageText,
                onValueChange = onMessageChange,
                placeholder = {
                    Text(
                        "Ask Biosense...",
                        color = Color.White.copy(alpha = 0.4f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color(0xFFE8E8ED),
                    unfocusedTextColor = Color(0xFFE8E8ED),
                    cursorColor = Color(0xFF1A4D5C),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Send Button
        val isEnabled = messageText.isNotBlank()

        IconButton(
            onClick = onSendClick,
            enabled = isEnabled,
            modifier = Modifier
                .size(56.dp)
                .glassEffect(shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send Message",
                tint = if (isEnabled) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
