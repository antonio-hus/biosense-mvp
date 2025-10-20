package com.biosense.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    data object Today : NavItem("today", Icons.Default.CalendarToday, "Today")
    data object Trends : NavItem("trends", Icons.Default.TrendingUp, "Trends")
    data object Chat : NavItem("chat", Icons.Default.Chat, "Chat")
    data object Search : NavItem("search", Icons.Default.Search, "Search")
}
