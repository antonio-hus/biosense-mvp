package com.biosense.app.data.model

import java.util.UUID

data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)