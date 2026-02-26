package com.el.studyflow.ui.techniques

import androidx.lifecycle.ViewModel
import com.el.studyflow.domain.model.StudyTechnique
import com.el.studyflow.domain.model.StudyTechniques
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TechniquesViewModel @Inject constructor() : ViewModel() {

    private val _techniques = MutableStateFlow(StudyTechniques.techniques)
    val techniques: StateFlow<List<StudyTechnique>> = _techniques.asStateFlow()

    fun toggleTechnique(techniqueId: String) {
        _techniques.update { techniques ->
            techniques.map { technique ->
                if (technique.id == techniqueId) {
                    technique.copy(isExpanded = !technique.isExpanded)
                } else {
                    technique.copy(isExpanded = false) // collapse others
                }
            }
        }
    }

    fun playVideo(techniqueId: String) {
        // TODO: Navigate to video player or open WebView
    }
}