package com.el.studyflow.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OnboardingState(
    val currentPage: Int = 0,
    // All answers keyed by question id — values are either String or Set<String>
    val answers: Map<String, Any> = emptyMap(),
    val isComplete: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    // ── Answer setters ─────────────────────────────────────────────────────────

    fun setSingleAnswer(id: String, value: String) {
        _state.update { it.copy(answers = it.answers + (id to value)) }
    }

    fun toggleMultiAnswer(id: String, value: String) {
        _state.update { current ->
            val existing = (current.answers[id] as? Set<*>)
                ?.filterIsInstance<String>()?.toMutableSet() ?: mutableSetOf()

            // "None of these" / "I'd rather not say" clears all other selections
            val exclusiveOptions = setOf("None of these", "I'd rather not say")
            val updated: Set<String> = when {
                value in exclusiveOptions -> setOf(value)
                value in existing -> existing - value
                else -> (existing - exclusiveOptions) + value
            }

            current.copy(answers = current.answers + (id to updated))
        }
    }

    fun setTextAnswer(id: String, value: String) {
        _state.update { it.copy(answers = it.answers + (id to value)) }
    }

    // ── Navigation ─────────────────────────────────────────────────────────────

    fun nextPage() {
        _state.update { current ->
            val sequence = buildQuestionSequence(current)
            if (current.currentPage < sequence.lastIndex) {
                current.copy(currentPage = current.currentPage + 1)
            } else {
                current.copy(isComplete = true)
            }
        }
    }

    fun previousPage() {
        _state.update { current ->
            if (current.currentPage > 0) {
                current.copy(currentPage = current.currentPage - 1)
            } else current
        }
    }

    fun completeSetup() {
        _state.update { it.copy(isComplete = true) }
    }
}