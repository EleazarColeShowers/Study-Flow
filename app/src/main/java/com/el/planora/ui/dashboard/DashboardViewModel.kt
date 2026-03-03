package com.el.planora.ui.dashboard

import androidx.lifecycle.ViewModel
import com.el.planora.domain.model.DashboardStats
import com.el.planora.domain.model.StudyTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _stats = MutableStateFlow(generateMockStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _studyTracks = MutableStateFlow(generateMockStudyTracks())
    val studyTracks: StateFlow<List<StudyTrack>> = _studyTracks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNewTrack() {
        // TODO: Navigate to create new study plan
    }

    companion object {
        private fun generateMockStats(): DashboardStats {
            return DashboardStats(
                dayStreak = 7,
                activePlans = 3,
                averageProgress = 68
            )
        }

        private fun generateMockStudyTracks(): List<StudyTrack> {
            return listOf(
                StudyTrack(
                    id = "1",
                    title = "Mathematics Tutorials",
                    exam = "JAMB Preparation",
                    subject = "Mathematics",
                    coverImageUrl = "", // placeholder
                    estimatedHours = 24,
                    completionPercentage = 68,
                    lastActiveDate = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000),
                    topics = listOf(
                        com.el.planora.domain.model.Topic(
                            id = "1",
                            name = "Algebra Basics",
                            estimatedHours = 4,
                            isCompleted = true,
                            resources = listOf("Video 1", "Practice 1")
                        ),
                        com.el.planora.domain.model.Topic(
                            id = "2",
                            name = "Geometry",
                            estimatedHours = 5,
                            isCompleted = false,
                            resources = listOf("Video 2")
                        )
                    )
                ),
                StudyTrack(
                    id = "2",
                    title = "English Literature",
                    exam = "WAEC Examination",
                    subject = "English",
                    coverImageUrl = "",
                    estimatedHours = 20,
                    completionPercentage = 45,
                    lastActiveDate = System.currentTimeMillis() - (5 * 24 * 60 * 60 * 1000),
                    topics = emptyList()
                ),
                StudyTrack(
                    id = "3",
                    title = "Biology Fundamentals",
                    exam = "University Entrance",
                    subject = "Biology",
                    coverImageUrl = "",
                    estimatedHours = 18,
                    completionPercentage = 52,
                    lastActiveDate = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000),
                    topics = emptyList()
                )
            )
        }
    }
}