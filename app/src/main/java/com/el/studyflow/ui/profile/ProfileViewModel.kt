package com.el.studyflow.ui.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ProfileUiState(
    val username: String = "Cole Showers",       // hardcoded until backend wired
    val email: String = "cole@example.com",
    val studySubject: String = "Mathematics",
    val studyGoal: String = "JAMB",
    val dailyDedication: String = "1 hour",
    val studyTime: String = "Morning",
    val attentionSpan: String = "30 minutes",
    val learningDifferences: List<String> = listOf("ADHD"),
    val isLoggingOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    // TODO: wire logout to AuthRepository when backend is connected
    fun logout(onLoggedOut: () -> Unit) {
        onLoggedOut()
    }
}