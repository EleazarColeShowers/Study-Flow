package com.el.planora.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class BottomNavTab(val icon: ImageVector, val label: String) {
    DASHBOARD(Icons.Default.Home, "Dashboard"),
    TECHNIQUES(Icons.Default.School, "Techniques"),
    CALENDAR(Icons.Default.DateRange, "Calendar"),
    CHECK_IN(Icons.Default.Chat, "Check-in"),
    TIMER(Icons.Default.Timer, "Timer")
}

@Composable
fun BottomNavBar(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val inactiveColor = if (isDark) Color.White.copy(alpha = 0.4f) else Color(0xFF555555)
    val activeColor = Color(0xFF40916C)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(backgroundColor),
        contentAlignment = androidx.compose.ui.Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab.values().forEach { tab ->
                BottomNavItem(
                    tab = tab,
                    isSelected = tab == currentTab,
                    onSelect = { onTabSelected(tab) },
                    activeColor = activeColor,
                    inactiveColor = inactiveColor
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onSelect: () -> Unit,
    activeColor: Color,
    inactiveColor: Color
) {
    val itemColor = if (isSelected) activeColor else inactiveColor

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = !isSelected, onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = itemColor,
            modifier = Modifier.size(24.dp)
        )
    }
}