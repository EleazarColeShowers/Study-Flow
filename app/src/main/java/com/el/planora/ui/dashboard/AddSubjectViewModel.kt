package com.el.planora.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.repository.AddSubjectRepository
import com.el.planora.data.repository.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddSubjectState(
    val subjectName: String = "",
    val contentType: String = "",       // theory | calculation | mixed | practical
    val memoryLoad: String = "",        // high | medium | low
    val difficulty: Int = 0,            // 1–5, 0 = not selected
    val hasDeadline: Boolean = false,
    val daysToExam: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val canSubmit: Boolean get() =
        subjectName.isNotBlank() &&
                contentType.isNotEmpty() &&
                memoryLoad.isNotEmpty() &&
                difficulty in 1..5 &&
                (!hasDeadline || daysToExam.toIntOrNull()?.let { it > 0 } == true)
}

@HiltViewModel
class AddSubjectViewModel @Inject constructor(
    private val addSubjectRepository: AddSubjectRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddSubjectState())
    val state: StateFlow<AddSubjectState> = _state.asStateFlow()

    fun onSubjectNameChange(value: String) =
        _state.update { it.copy(subjectName = value, error = null) }

    fun onContentTypeChange(value: String) =
        _state.update { it.copy(contentType = value, error = null) }

    fun onMemoryLoadChange(value: String) =
        _state.update { it.copy(memoryLoad = value, error = null) }

    fun onDifficultyChange(value: Int) =
        _state.update { it.copy(difficulty = value, error = null) }

    fun onHasDeadlineChange(value: Boolean) =
        _state.update { it.copy(hasDeadline = value, daysToExam = "", error = null) }

    fun onDaysToExamChange(value: String) =
        _state.update { it.copy(daysToExam = value.filter { it.isDigit() }, error = null) }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        if (!s.canSubmit) return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            when (val result = addSubjectRepository.addSubject(
                subjectName = s.subjectName,
                contentType = s.contentType,
                memoryLoad  = s.memoryLoad,
                difficulty  = s.difficulty,
                hasDeadline = s.hasDeadline,
                daysToExam  = s.daysToExam.toIntOrNull() ?: 999
            )) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isSaving = false, isSuccess = true) }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isSaving = false, error = result.message) }
                }
                else -> Unit
            }
        }
    }

    fun reset() { _state.value = AddSubjectState() }
}