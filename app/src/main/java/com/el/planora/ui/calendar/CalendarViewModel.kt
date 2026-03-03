package com.el.planora.ui.calendar

import androidx.lifecycle.ViewModel
import com.el.planora.domain.model.CalendarDay
import com.el.planora.domain.model.DayStatus
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

    private val _calendarDays = MutableStateFlow(generateCalendarDays(YearMonth.now()))
    val calendarDays: StateFlow<List<CalendarDay>> = _calendarDays.asStateFlow()

    fun previousMonth() {
        // FIX: update _currentMonth first, then read the new value to generate days
        _currentMonth.update { it.minusMonths(1) }
        _calendarDays.value = generateCalendarDays(_currentMonth.value)
    }

    fun nextMonth() {
        _currentMonth.update { it.plusMonths(1) }
        _calendarDays.value = generateCalendarDays(_currentMonth.value)
    }

    companion object {
        // FIX: was always hardcoded to today's month — now takes the displayed month as param
        fun generateCalendarDays(month: YearMonth): List<CalendarDay> {
            val today = LocalDate.now()

            return (1..month.lengthOfMonth()).map { day ->
                val date = month.atDay(day)
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