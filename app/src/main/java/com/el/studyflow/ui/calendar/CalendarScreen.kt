package com.el.studyflow.ui.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.studyflow.domain.model.CalendarDay
import com.el.studyflow.domain.model.DayStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val bgColor       = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg        = if (isDark) Color(0xFF161616) else Color(0xFFFFFFFF)
    val surfaceBg     = if (isDark) Color(0xFF1E1E1E) else Color(0xFFEAF4EE)
    val textColor     = if (isDark) Color(0xFFEEEEEE) else Color(0xFF111111)
    val subtleText    = if (isDark) Color(0xFF888888) else Color(0xFF888888)
    val borderColor   = if (isDark) Color(0xFF2A2A2A) else Color(0xFFDDEDE5)
    val accentGreen   = Color(0xFF40916C)
    val accentBlue    = Color(0xFF3B82F6)
    val today         = LocalDate.now()

    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val calendarDays by viewModel.calendarDays.collectAsStateWithLifecycle()

    // Stats
    val completed = calendarDays.count { it.status == DayStatus.COMPLETED }
    val missed    = calendarDays.count { it.status == DayStatus.MISSED }
    val scheduled = calendarDays.count { it.status == DayStatus.SCHEDULED }
    val total     = completed + missed
    val rate      = if (total > 0) (completed * 100 / total) else 0

    // FIX: Java's DayOfWeek is ISO (Mon=1, Sun=7). Headers start Sunday, so
    // convert: Sun→0, Mon→1, …, Sat→6  using  (isoValue % 7)
    val paddingDays = calendarDays.firstOrNull()
        ?.date?.dayOfWeek?.value
        ?.let { it % 7 }   // Mon=1%7=1, …, Sat=6%7=6, Sun=7%7=0
        ?: 0

    val allCells: List<CalendarDay?> = List(paddingDays) { null } + calendarDays

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ──────────────────────────────────────────────
            Text(
                text = "Study Calendar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = subtleText,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(20.dp))

            // ── Stats row ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "$rate%",
                    label = "Rate",
                    valueColor = accentGreen,
                    bg = surfaceBg,
                    border = borderColor
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "$completed",
                    label = "Done",
                    valueColor = accentGreen,
                    bg = surfaceBg,
                    border = borderColor
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "$scheduled",
                    label = "Upcoming",
                    valueColor = accentBlue,
                    bg = surfaceBg,
                    border = borderColor
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    value = "$missed",
                    label = "Missed",
                    valueColor = subtleText,
                    bg = surfaceBg,
                    border = borderColor
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Calendar card ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Column {

                    // Month navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NavButton(onClick = viewModel::previousMonth, isDark = isDark) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = accentGreen)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM")),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                text = currentMonth.year.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = subtleText
                            )
                        }

                        NavButton(onClick = viewModel::nextMonth, isDark = isDark) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = accentGreen)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Day-of-week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                            Text(
                                text = label,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = subtleText
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Thin divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(borderColor)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Grid rows
                    val totalCells = allCells.size
                    val rows = (totalCells + 6) / 7  // ceil division

                    for (week in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0 until 7) {
                                val index = week * 7 + col
                                val day = allCells.getOrNull(index)
                                val isToday = day?.date == today

                                CalendarDayCell(
                                    day = day,
                                    isToday = isToday,
                                    textColor = textColor,
                                    subtleText = subtleText,
                                    accentGreen = accentGreen,
                                    accentBlue = accentBlue,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Legend ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .background(cardBg)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = accentGreen, label = "Completed", textColor = subtleText)
                LegendDivider(borderColor)
                LegendItem(color = accentBlue, label = "Scheduled", textColor = subtleText)
                LegendDivider(borderColor)
                LegendItem(color = subtleText, label = "Missed", textColor = subtleText)
                LegendDivider(borderColor)
                LegendItem(
                    color = Color.Transparent,
                    label = "Today",
                    textColor = subtleText,
                    isTodayIndicator = true,
                    todayRingColor = accentGreen
                )
            }
        }
    }
}

// ── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun CalendarDayCell(
    day: CalendarDay?,
    isToday: Boolean,
    textColor: Color,
    subtleText: Color,
    accentGreen: Color,
    accentBlue: Color,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        day == null -> Color.Transparent
        isToday -> accentGreen
        day.status == DayStatus.COMPLETED -> accentGreen.copy(alpha = 0.18f)
        day.status == DayStatus.SCHEDULED -> accentBlue.copy(alpha = 0.15f)
        day.status == DayStatus.MISSED -> Color.Gray.copy(alpha = 0.13f)
        else -> Color.Transparent
    }

    val numberColor = when {
        day == null -> Color.Transparent
        isToday -> Color.White
        day.status == DayStatus.EMPTY -> subtleText.copy(alpha = 0.5f)
        else -> textColor
    }

    val dotColor = when {
        isToday || day == null -> Color.Transparent
        day.status == DayStatus.COMPLETED -> accentGreen
        day.status == DayStatus.SCHEDULED -> accentBlue
        day.status == DayStatus.MISSED -> Color.Gray
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (day != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                    color = numberColor,
                    fontSize = 13.sp
                )
                if (dotColor != Color.Transparent) {
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    valueColor: Color,
    bg: Color,
    border: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .background(bg)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = valueColor.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun NavButton(
    onClick: () -> Unit,
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    val bg = if (isDark) Color(0xFF252525) else Color(0xFFEAF4EE)
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
    ) {
        content()
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    textColor: Color,
    isTodayIndicator: Boolean = false,
    todayRingColor: Color = Color.Transparent
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (isTodayIndicator) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, todayRingColor, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun LegendDivider(color: Color) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(16.dp)
            .background(color)
    )
}