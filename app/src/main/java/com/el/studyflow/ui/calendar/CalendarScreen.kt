package com.el.studyflow.ui.calendar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.studyflow.domain.model.CalendarDay
import com.el.studyflow.domain.model.DayStatus
import java.time.YearMonth

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0A0A0A) else Color(0xFFF4FBF7)
    val cardBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFE8F3ED)
    val textColor = if (isDark) Color.White else Color(0xFF0A0A0A)
    val subtleText = if (isDark) Color.White.copy(alpha = 0.6f) else Color(0xFF555555)
    val accentGreen = Color(0xFF40916C)

    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val calendarDays by viewModel.calendarDays.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "Previous month",
                        tint = accentGreen
                    )
                }

                Text(
                    text = currentMonth.format(
                        java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy")
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentGreen
                )

                IconButton(onClick = viewModel::nextMonth) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Next month",
                        tint = accentGreen
                    )
                }
            }

            // Calendar grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .padding(16.dp)
            ) {
                Column {
                    // Day of week headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = subtleText,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar days grid
                    val daysInGrid = 35 // 5 rows x 7 days
                    val paddingDays = calendarDays.firstOrNull()?.date?.dayOfWeek?.value?.minus(1) ?: 0

                    val allDays = (0 until paddingDays).map { null } + calendarDays

                    for (week in 0 until 6) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (dayOfWeek in 0 until 7) {
                                val index = week * 7 + dayOfWeek
                                val day = if (index < allDays.size) allDays[index] else null

                                if (day != null) {
                                    CalendarDayBox(
                                        day = day,
                                        textColor = textColor,
                                        subtleText = subtleText,
                                        accentGreen = accentGreen
                                    )
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardBg)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(
                    color = accentGreen,
                    label = "Completed",
                    textColor = textColor,
                    subtleText = subtleText
                )
                LegendItem(
                    color = Color(0xFF0066CC),
                    label = "Scheduled",
                    textColor = textColor,
                    subtleText = subtleText
                )
                LegendItem(
                    color = Color.Gray,
                    label = "Missed",
                    textColor = textColor,
                    subtleText = subtleText
                )
            }
        }
    }
}

@Composable
private fun CalendarDayBox(
    day: CalendarDay,
    textColor: Color,
    subtleText: Color,
    accentGreen: Color,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (day.status) {
        DayStatus.COMPLETED -> accentGreen.copy(alpha = 0.3f)
        DayStatus.SCHEDULED -> Color(0xFF0066CC).copy(alpha = 0.2f)
        DayStatus.MISSED -> Color.Gray.copy(alpha = 0.2f)
        DayStatus.EMPTY -> Color.Transparent
    }

    val dotColor = when (day.status) {
        DayStatus.COMPLETED -> accentGreen
        DayStatus.SCHEDULED -> Color(0xFF0066CC)
        DayStatus.MISSED -> Color.Gray
        DayStatus.EMPTY -> Color.Transparent
    }

    Box(
        modifier = modifier
//            .weight(1f)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (day.status != DayStatus.EMPTY) backgroundColor else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (day.status != DayStatus.EMPTY) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(dotColor)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    textColor: Color,
    subtleText: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = subtleText
        )
    }
}