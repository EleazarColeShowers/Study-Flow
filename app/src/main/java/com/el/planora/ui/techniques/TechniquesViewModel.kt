package com.el.planora.ui.techniques

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.remote.model.RecommendationItem
import com.el.planora.data.repository.ApiResult
import com.el.planora.data.repository.RecommendationsRepository
import com.el.planora.domain.model.StudyTechnique
import com.el.planora.domain.model.StudyTechniques
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TechniquesUiState(
    val techniques: List<StudyTechnique> = emptyList(),
    val sessionLength: String = "",
    val dailySessions: String = "",
    val subjectName: String = "",
    val isLoading: Boolean = true,
    val isFromApi: Boolean = false,   // true = AI recommendations, false = hardcoded fallback
    val error: String? = null
)

@HiltViewModel
class TechniquesViewModel @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TechniquesUiState())
    val uiState: StateFlow<TechniquesUiState> = _uiState.asStateFlow()

    // Keep this for backward compat with existing screen collectAsStateWithLifecycle calls
    val techniques: StateFlow<List<StudyTechnique>> get() = MutableStateFlow(_uiState.value.techniques)

    init {
        loadRecommendations()
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = recommendationsRepository.getRecommendations()) {
                is ApiResult.Success -> {
                    val response = result.data
                    val apiTechniques = response.recommendations
                        ?.mapNotNull { it.toStudyTechnique() }
                        ?: emptyList()

                    if (apiTechniques.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                techniques    = apiTechniques,
                                sessionLength = response.sessionLength ?: "",
                                dailySessions = response.dailySessions ?: "",
                                subjectName   = response.subject ?: "",
                                isLoading     = false,
                                isFromApi     = true
                            )
                        }
                    } else {
                        // API returned empty — fall back to hardcoded
                        useFallback()
                    }
                }
                is ApiResult.Error -> {
                    // API failed — show hardcoded list so screen is never empty
                    useFallback(error = result.message)
                }
                else -> Unit
            }
        }
    }

    private fun useFallback(error: String? = null) {
        _uiState.update {
            it.copy(
                techniques = StudyTechniques.techniques,
                isLoading  = false,
                isFromApi  = false,
                error      = error
            )
        }
    }

    fun retry() {
        loadRecommendations()
    }

    fun toggleTechnique(techniqueId: String) {
        _uiState.update { state ->
            state.copy(
                techniques = state.techniques.map { technique ->
                    if (technique.id == techniqueId) {
                        technique.copy(isExpanded = !technique.isExpanded)
                    } else {
                        technique.copy(isExpanded = false)
                    }
                }
            )
        }
    }

    fun playVideo(techniqueId: String) {
        // TODO: Navigate to video player or open WebView
    }
}

// ── Map API recommendation to StudyTechnique ──────────────────────────────────
private fun RecommendationItem.toStudyTechnique(): StudyTechnique? {
    val name = name ?: return null
    val howTo = howTo ?: return null
    val why = why ?: return null

    // Assign a colour and emoji per rank so cards look distinct
    val (color, icon) = when (rank) {
        1    -> "#40916C" to "🧠"
        2    -> "#0066CC" to "🎯"
        3    -> "#FF9500" to "⏰"
        4    -> "#FF3B30" to "🗺️"
        5    -> "#E53935" to "🍅"
        else -> "#40916C" to "📚"
    }

    // Confidence badge text e.g. "91% match"
    val confidenceText = confidence?.let { " · ${(it * 100).toInt()}% match" } ?: ""

    return StudyTechnique(
        id                  = id ?: rank.toString() ?: "0",
        name                = name,
        description         = why.take(80) + if (why.length > 80) "…" else "",
        whyItWorks          = why,
        icon                = icon,
        iconBackgroundColor = color,
        howToApply          = howTo,
        videoUrl            = null
    )
}