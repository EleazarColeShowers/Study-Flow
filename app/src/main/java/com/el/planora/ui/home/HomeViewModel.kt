package com.el.planora.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.repository.ApiResult
import com.el.planora.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentPage: Int = 0,
    // All answers keyed by question id — values are either String or Set<String>
    val answers: Map<String, Any> = emptyMap(),
    val isComplete: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository  // ← injected
) : ViewModel() {

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

            val exclusiveOptions = setOf("None of these", "I'd rather not say")
            val updated: Set<String> = when {
                value in exclusiveOptions -> setOf(value)
                value in existing         -> existing - value
                else                      -> (existing - exclusiveOptions) + value
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
            if (current.currentPage > 0) current.copy(currentPage = current.currentPage - 1)
            else current
        }
    }

    // ── Complete setup — save to Firestore then mark complete ──────────────────
    fun completeSetup() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveError = null) }

            when (val result = userProfileRepository.saveOnboardingProfile(_state.value.answers)) {
                is ApiResult.Success -> {
                    // Profile saved — navigate forward
                    _state.update { it.copy(isSaving = false, isComplete = true) }
                }
                is ApiResult.Error -> {
                    // Save failed — still let user through, they can retry later
                    // Profile save failure should not block app access
                    _state.update {
                        it.copy(
                            isSaving = false,
                            saveError = result.message,
                            isComplete = true   // still proceed
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}