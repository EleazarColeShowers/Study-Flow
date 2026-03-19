package com.el.planora.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.local.dao.CheckInSummaryDao
import com.el.planora.domain.model.CalendarDay
import com.el.planora.domain.model.DayStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val checkInSummaryDao: CheckInSummaryDao
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _calendarDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val calendarDays: StateFlow<List<CalendarDay>> = _calendarDays.asStateFlow()

    // All session dates from Room — keyed by "YYYY-MM-DD"
    private var completedDates: Set<String> = emptySet()
    private var reviewDueDates: Set<String> = emptySet()

    init {
        loadSessionData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            // Collect all check-in summaries from Room
            checkInSummaryDao.getAllSummaries().collect { summaries ->
                val today = LocalDate.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                // Sessions that were completed (savedAt → local date)
                completedDates = summaries.map { summary ->
                    LocalDate.ofEpochDay(summary.savedAt / 86_400_000).format(formatter)
                }.toSet()

                // Next review dates scheduled for future
                reviewDueDates = summaries
                    .map { it.nextReviewDate }
                    .filter { dateStr ->
                        runCatching {
                            LocalDate.parse(dateStr, formatter).isAfter(today)
                        }.getOrDefault(false)
                    }
                    .toSet()

                // Rebuild the current month with real data
                rebuildCalendar()
            }
        }
    }

    private fun rebuildCalendar() {
        _calendarDays.value = generateCalendarDays(_currentMonth.value)
    }

    fun previousMonth() {
        _currentMonth.update { it.minusMonths(1) }
        rebuildCalendar()
    }

    fun nextMonth() {
        _currentMonth.update { it.plusMonths(1) }
        rebuildCalendar()
    }

    private fun generateCalendarDays(month: YearMonth): List<CalendarDay> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            val dateStr = date.format(formatter)

            val status = when {
                date.isAfter(today) && dateStr in reviewDueDates -> DayStatus.SCHEDULED
                date.isAfter(today)                              -> DayStatus.EMPTY
                dateStr in completedDates                        -> DayStatus.COMPLETED
                date.isBefore(today)                            -> DayStatus.MISSED
                else                                            -> DayStatus.EMPTY
            }

            CalendarDay(date, status)
        }
    }
}
