package com.el.studyflow.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject



data class SetupState(
    val currentPage: Int = 0,
    // Q1 - multi-select
    val selectedLearningDiffs: Set<String> = emptySet(),
    // Q2-Q5 - single select
    val selectedAttentionSpan: String = "",
    val selectedStudyTime: String = "",
    val selectedDailyDedication: String = "",
    val selectedStudyGoal: String = "",
    val isComplete: Boolean = false,
    val textAnswer: String = ""


) {
    val canGoNext: Boolean get() = when (currentPage) {
        0 -> selectedLearningDiffs.isNotEmpty()
        1 -> selectedAttentionSpan.isNotEmpty()
        2 -> selectedStudyTime.isNotEmpty()
        3 -> selectedDailyDedication.isNotEmpty()
        4 -> selectedStudyGoal.isNotEmpty()
        5 -> textAnswer.isNotBlank()
        else -> false
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(SetupState())
    val state: StateFlow<SetupState> = _state.asStateFlow()

    // Q1 toggles (multi-select — except "None" clears others)
    fun toggleLearningDiff(option: String) {
        _state.update { current ->
            val updated = if (option == "None") {
                setOf("None")
            } else {
                val without = current.selectedLearningDiffs - "None"
                if (option in without) without - option else without + option
            }
            current.copy(selectedLearningDiffs = updated)
        }
    }

    // Q2-Q5 single selects
    fun selectAttentionSpan(option: String) =
        _state.update { it.copy(selectedAttentionSpan = option) }

    fun selectStudyTime(option: String) =
        _state.update { it.copy(selectedStudyTime = option) }

    fun selectDailyDedication(option: String) =
        _state.update { it.copy(selectedDailyDedication = option) }

    fun selectStudyGoal(option: String) =
        _state.update { it.copy(selectedStudyGoal = option) }

    fun nextPage() {
        _state.update { current ->
            if (current.currentPage < 5) {
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

    fun updateTextAnswer(value: String) {
        _state.update { it.copy(textAnswer = value) }
    }
}