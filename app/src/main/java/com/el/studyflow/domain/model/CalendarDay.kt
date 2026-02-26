package com.el.studyflow.domain.model

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate,
    val status: DayStatus, // COMPLETED, SCHEDULED, MISSED, EMPTY
    val hoursStudied: Int = 0
)

enum class DayStatus {
    COMPLETED, SCHEDULED, MISSED, EMPTY
}

data class PomodoroSession(
    val id: String,
    val name: String,
    val durationMinutes: Int,
    val quotation: String,
    val author: String,
    val isRunning: Boolean = false,
    val elapsedSeconds: Int = 0
)

data class DailyCheckIn(
    val id: String,
    val date: Long,
    val sessionQuality: SessionQuality? = null,
    val timestamp: String // "2:30 PM"
)

enum class SessionQuality {
    GREAT, OKAY, STRUGGLED
}

// Mock quotations for Pomodoro sessions
object Quotations {
    val quotes = listOf(
        "Success is the sum of small efforts repeated day in and day out." to "Robert Collier",
        "The expert in anything was once a beginner." to "Helen Hayes",
        "Discipline is choosing between what you want now and what you want most." to "Abraham Lincoln",
        "Knowledge is power." to "Francis Bacon",
        "The only way to do great work is to love what you do." to "Steve Jobs",
        "Education is the most powerful weapon which you can use to change the world." to "Nelson Mandela",
        "Learning never exhausts the mind." to "Leonardo da Vinci",
        "In learning you will teach, and in teaching you will learn." to "Phil Collins"
    )

    fun random() = quotes.random()
}