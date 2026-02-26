package com.el.studyflow.ui.checkin

import androidx.lifecycle.ViewModel
import com.el.studyflow.domain.model.DailyCheckIn
import com.el.studyflow.domain.model.SessionQuality
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CheckInViewModel @Inject constructor() : ViewModel() {

    private val _checkInState = MutableStateFlow(CheckInUiState())
    val checkInState: StateFlow<CheckInUiState> = _checkInState.asStateFlow()

    fun submitCheckIn(quality: SessionQuality) {
        _checkInState.update {
            it.copy(
                selectedQuality = quality,
                isSubmitting = true
            )
        }
        // Simulate save
        // In reality, this would call a use case to save to Firestore
        _checkInState.update {
            it.copy(
                isSubmitting = false,
                isCompleted = true
            )
        }
    }

    fun reset() {
        _checkInState.value = CheckInUiState()
    }
}

data class CheckInUiState(
    val question: String = "How did today's study session go?",
    val timestamp: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
    val selectedQuality: SessionQuality? = null,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false
)