package com.biosense.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val icon: ImageVector,
    val title: String,
    val color: Color
) {
    data object Today : NavItem(
        route = "today",
        icon = Icons.Default.Home,
        title = "Home",
        color = Color(0xFF64B5F6) // Blue
    )

    data object Challenges : NavItem(
        route = "challenges",
        icon = Icons.Default.EmojiEvents,
        title = "Challenges",
        color = Color(0xFFFFC107) // Amber/Gold
    )

    data object Chat : NavItem(
        route = "chat",
        icon = Icons.Default.Chat,
        title = "Advisor",
        color = Color(0xFF9C27B0) // Purple
    )
}
