package com.el.studyflow.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.studyflow.domain.model.DailyCheckIn
import com.el.studyflow.domain.model.SessionQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

// ── Model ────────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
)

data class CheckInUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Hey! 👋 How did today's study session go?",
            isUser = false
        )
    ),
    val selectedQuality: SessionQuality? = null,
    val isBotTyping: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false
)

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class CheckInViewModel @Inject constructor() : ViewModel() {

    private val _checkInState = MutableStateFlow(CheckInUiState())
    val checkInState: StateFlow<CheckInUiState> = _checkInState.asStateFlow()

    fun submitCheckIn(quality: SessionQuality) {
        if (_checkInState.value.selectedQuality != null) return // prevent double-tap

        val userText = when (quality) {
            SessionQuality.GREAT     -> "Great! 🎉"
            SessionQuality.OKAY      -> "Okay 👍"
            SessionQuality.STRUGGLED -> "Struggled 😓"
        }

        // 1. Add user bubble + mark selected
        _checkInState.update {
            it.copy(
                messages = it.messages + ChatMessage(text = userText, isUser = true),
                selectedQuality = quality,
                isSubmitting = true
            )
        }

        viewModelScope.launch {
            // 2. Show typing indicator
            delay(400)
            _checkInState.update { it.copy(isBotTyping = true, isSubmitting = false) }

            // 3. Simulate bot response delay
            delay(1200)

            val botReply = when (quality) {
                SessionQuality.GREAT ->
                    "That's awesome! 🌟 Keep up the great work — consistency is the key to mastery."
                SessionQuality.OKAY ->
                    "Solid effort! 💪 Every session counts. Tomorrow is another chance to push further."
                SessionQuality.STRUGGLED ->
                    "That's okay — struggling means you're challenging yourself. Take a break and come back fresh! 🌱"
            }

            // 4. Replace typing indicator with bot reply and mark complete
            _checkInState.update {
                it.copy(
                    messages = it.messages + ChatMessage(text = botReply, isUser = false),
                    isBotTyping = false,
                    isCompleted = true
                )
            }
        }
    }

    fun reset() {
        _checkInState.value = CheckInUiState()
    }
}