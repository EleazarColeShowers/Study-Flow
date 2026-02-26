package com.el.studyflow.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.studyflow.domain.model.PomodoroSession
import com.el.studyflow.domain.model.Quotations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor() : ViewModel() {

    private var timerJob: Job? = null

    private val _session = MutableStateFlow(generateNewSession())
    val session: StateFlow<PomodoroSession> = _session.asStateFlow()

    fun startSession() {
        _session.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_session.value.isRunning) {
                delay(1000)
                _session.update { currentSession ->
                    val newElapsed = currentSession.elapsedSeconds + 1
                    val totalSeconds = currentSession.durationMinutes * 60

                    if (newElapsed >= totalSeconds) {
                        // Session complete
                        currentSession.copy(
                            isRunning = false,
                            elapsedSeconds = totalSeconds
                        )
                    } else {
                        currentSession.copy(elapsedSeconds = newElapsed)
                    }
                }
            }
        }
    }

    fun pauseSession() {
        _session.update { it.copy(isRunning = false) }
        timerJob?.cancel()
    }

    fun resumeSession() {
        startSession()
    }

    fun endSession() {
        timerJob?.cancel()
        _session.value = generateNewSession()
    }

    fun getTimeDisplay(): String {
        val session = _session.value
        val totalSeconds = session.durationMinutes * 60
        val remainingSeconds = totalSeconds - session.elapsedSeconds
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    companion object {
        private fun generateNewSession(): PomodoroSession {
            val (quote, author) = Quotations.random()
            return PomodoroSession(
                id = System.currentTimeMillis().toString(),
                name = "Pomodoro Session",
                durationMinutes = 25,
                quotation = quote,
                author = author,
                isRunning = false,
                elapsedSeconds = 0
            )
        }
    }
}