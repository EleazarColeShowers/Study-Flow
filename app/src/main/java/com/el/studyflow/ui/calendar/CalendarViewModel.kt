package com.el.studyflow.ui.calendar

import androidx.lifecycle.ViewModel
import com.el.studyflow.domain.model.CalendarDay
import com.el.studyflow.domain.model.DayStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor() : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _calendarDays = MutableStateFlow(generateMockCalendarDays())
    val calendarDays: StateFlow<List<CalendarDay>> = _calendarDays.asStateFlow()

    fun previousMonth() {
        _currentMonth.update { it.minusMonths(1) }
        _calendarDays.value = generateMockCalendarDays()
    }

    fun nextMonth() {
        _currentMonth.update { it.plusMonths(1) }
        _calendarDays.value = generateMockCalendarDays()
    }

    companion object {
        private fun generateMockCalendarDays(): List<CalendarDay> {
            val today = LocalDate.now()
            val daysInMonth = today.lengthOfMonth()

            return (1..daysInMonth).map { day ->
                val date = LocalDate.of(today.year, today.month, day)
                val status = when {
                    date == today -> DayStatus.COMPLETED
                    date.isAfter(today) -> DayStatus.SCHEDULED
                    date.dayOfMonth % 3 == 0 -> DayStatus.COMPLETED
                    date.dayOfMonth % 5 == 0 -> DayStatus.SCHEDULED
                    else -> DayStatus.EMPTY
                }
                CalendarDay(date, status)
            }
        }
    }
}