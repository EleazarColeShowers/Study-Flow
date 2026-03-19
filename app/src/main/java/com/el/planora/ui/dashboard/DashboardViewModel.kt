package com.el.planora.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.repository.ApiResult
import com.el.planora.data.repository.SubjectsRepository
import com.el.planora.data.repository.UserRegistrationRepository
import com.el.planora.domain.model.DashboardStats
import com.el.planora.domain.model.StudyTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val subjectsRepository: SubjectsRepository,
    private val userRegistrationRepository: UserRegistrationRepository  // ← add this

) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats(0, 0, 0))
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _allTracks = MutableStateFlow<List<StudyTrack>>(emptyList())

    private val _studyTracks = MutableStateFlow<List<StudyTrack>>(emptyList())
    val studyTracks: StateFlow<List<StudyTrack>> = _studyTracks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            userRegistrationRepository.registerWithPlanora()


            when (val result = subjectsRepository.getSubjects()) {
                is ApiResult.Success -> {
                    val tracks = result.data
                    android.util.Log.d("Dashboard", "Tracks loaded: ${tracks.size}")
                    _allTracks.value = tracks
                    _studyTracks.value = tracks
                    _stats.value = subjectsRepository.calculateStats(tracks)
                    _uiState.update { it.copy(isLoading = false) }
                }
                is ApiResult.Error -> {
                    android.util.Log.d("Dashboard", "Error: ${result.message}")
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> Unit
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _studyTracks.value = if (query.isBlank()) {
            _allTracks.value
        } else {
            _allTracks.value.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.subject.contains(query, ignoreCase = true)
            }
        }
    }

    fun refresh() {
        loadSubjects()
    }

    fun addNewTrack() {
        // TODO: Navigate to Add Subject screen
    }
}