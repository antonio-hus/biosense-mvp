package com.biosense.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biosense.app.ui.theme.InterFontFamily

@Composable
fun Logo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    fontSize: TextUnit = 20.sp,
    color: Color = Color.Black
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Eco,
            contentDescription = "Biosense Logo",
            modifier = Modifier.size(iconSize),
            tint = color
        )
        Text(
            text = "biosense",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize,
            color = color
        )
    }
}
