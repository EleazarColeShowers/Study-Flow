package com.el.planora.ui.subject

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.local.dao.CompletedSessionDao
import com.el.planora.data.local.entity.CompletedSessionEntity
import com.el.planora.data.remote.planoraApiService
import com.el.planora.data.remote.model.RecommendationItem
import com.el.planora.data.repository.ApiResult
import com.el.planora.data.repository.RecommendationsRepository
import com.el.planora.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ── UI models ─────────────────────────────────────────────────────────────────

data class StudySession(
    val index: Int,
    val startTime: String,        // "2:00 PM"
    val endTime: String,          // "2:45 PM"
    val durationMinutes: Int,
    val title: String,            // e.g. "Read & Review"
    val technique: String,        // e.g. "Active Recall"
    val sessionKey: String,       // for Room completion tracking
    val isCompleted: Boolean = false
)

data class DueFlashcard(
    val id: String,
    val front: String,
    val back: String
)

data class SubjectDetailUiState(
    val isLoading: Boolean = true,
    val subjectName: String = "",
    val subjectId: String = "",
    val contentType: String = "",
    val daysToExam: Int = 0,
    val sessionLength: String = "",      // "15 minutes per session"
    val dailySessions: String = "",      // "3-4 sessions per day"
    val todaySessions: List<StudySession> = emptyList(),
    val techniques: List<RecommendationItem> = emptyList(),
    val dueFlashcards: List<DueFlashcard> = emptyList(),
    val completedSessionKeys: Set<String> = emptySet(),
    val totalCompleted: Int = 0,
    val error: String? = null,
    val today: String = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recommendationsRepository: RecommendationsRepository,
    private val userProfileRepository: UserProfileRepository,
    private val completedSessionDao: CompletedSessionDao,
    private val api: planoraApiService,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Subject info passed via navigation
    private val subjectId: String   = savedStateHandle["subjectId"] ?: ""
    private val subjectName: String = savedStateHandle["subjectName"] ?: ""
    private val contentType: String = savedStateHandle["contentType"] ?: ""
    private val daysToExam: Int     = savedStateHandle["daysToExam"] ?: 0

    private val _state = MutableStateFlow(SubjectDetailUiState(
        subjectId   = subjectId,
        subjectName = subjectName,
        contentType = contentType,
        daysToExam  = daysToExam
    ))
    val state: StateFlow<SubjectDetailUiState> = _state.asStateFlow()

    init {
        load()
        observeCompletions()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // ── Fetch user profile for peakFocusTime ──────────────────────────
            val profile = when (val r = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> r.data
                else -> null
            }

            // ── Fetch AI recommendations ──────────────────────────────────────
            val recommendResponse = when (val r = recommendationsRepository.getRecommendationsForSubject(
                subjectId   = subjectId,
                subjectName = subjectName,
                contentType = contentType,
                daysToExam  = daysToExam
            )) {
                is ApiResult.Success -> r.data
                else -> null
            }

            val techniques    = recommendResponse?.recommendations ?: emptyList()
            val sessionLength = recommendResponse?.sessionLength ?: "25 minutes per session"
            val dailySessions = recommendResponse?.dailySessions ?: "2-3 sessions per day"

            // ── Parse session duration ─────────────────────────────────────────
            val durationMinutes = sessionLength
                .replace(Regex("[^0-9]"), " ").trim()
                .split(" ").firstOrNull()?.toIntOrNull() ?: 25

            // ── Parse number of sessions ───────────────────────────────────────
            val sessionCount = dailySessions
                .replace(Regex("[^0-9\\-]"), " ").trim()
                .split("-").firstOrNull()?.toIntOrNull() ?: 2

            // ── Build time-blocked schedule from peakFocusTime ────────────────
            val startHour = when (profile?.peakFocusTime) {
                "morning"    -> 8
                "afternoon"  -> 13
                "evening"    -> 18
                "late_night" -> 21
                else         -> 14
            }

            val todaySessions = buildSchedule(
                subjectId       = subjectId,
                subjectName     = subjectName,
                sessionCount    = sessionCount,
                durationMinutes = durationMinutes,
                startHour       = startHour,
                techniques      = techniques
            )

            // ── Fetch due flashcards ───────────────────────────────────────────
            val planoraUserId = getPlanoraUserId() ?: ""
            val dueFlashcards = if (planoraUserId.isNotEmpty()) {
                fetchDueFlashcards(planoraUserId, subjectId)
            } else emptyList()

            // ── Total completed sessions for this subject ──────────────────────
            val totalCompleted = completedSessionDao.getTotalCompletedForSubject(subjectId)

            _state.update {
                it.copy(
                    isLoading     = false,
                    sessionLength = sessionLength,
                    dailySessions = dailySessions,
                    todaySessions = todaySessions,
                    techniques    = techniques.filterNotNull(),
                    dueFlashcards = dueFlashcards,
                    totalCompleted = totalCompleted
                )
            }
        }
    }

    // ── Observe completion state from Room reactively ─────────────────────────
    private fun observeCompletions() {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewModelScope.launch {
            completedSessionDao.getCompletedKeysForDate(subjectId, today).collect { keys ->
                _state.update { state ->
                    state.copy(
                        completedSessionKeys = keys.toSet(),
                        todaySessions = state.todaySessions.map { session ->
                            session.copy(isCompleted = session.sessionKey in keys)
                        }
                    )
                }
            }
        }
    }

    // ── Toggle session completion ──────────────────────────────────────────────
    fun toggleSession(session: StudySession) {
        viewModelScope.launch {
            if (session.isCompleted) {
                completedSessionDao.markIncomplete(session.sessionKey)
            } else {
                completedSessionDao.markComplete(
                    CompletedSessionEntity(
                        sessionKey   = session.sessionKey,
                        subjectId    = subjectId,
                        date         = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        sessionIndex = session.index
                    )
                )
            }
        }
    }

    // ── Build time-blocked schedule ────────────────────────────────────────────
    private fun buildSchedule(
        subjectId: String,
        subjectName: String,
        sessionCount: Int,
        durationMinutes: Int,
        startHour: Int,
        techniques: List<RecommendationItem?>
    ): List<StudySession> {
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val breakMinutes = 10

        val sessionTitles = listOf(
            "Study & Review",
            "Practice & Apply",
            "Recall & Test",
            "Deep Focus",
            "Revision"
        )

        return (0 until sessionCount).map { i ->
            val minutesOffset = i * (durationMinutes + breakMinutes)
            val start = LocalTime.of(startHour, 0).plusMinutes(minutesOffset.toLong())
            val end   = start.plusMinutes(durationMinutes.toLong())

            val technique = techniques.getOrNull(i % techniques.size)?.name ?: "Focused Study"
            val title     = sessionTitles.getOrElse(i) { "Study Session ${i + 1}" }

            StudySession(
                index           = i,
                startTime       = start.format(timeFormatter),
                endTime         = end.format(timeFormatter),
                durationMinutes = durationMinutes,
                title           = title,
                technique       = technique,
                sessionKey      = "${subjectId}_${today}_$i"
            )
        }
    }

    // ── Fetch due flashcards ───────────────────────────────────────────────────
    private suspend fun fetchDueFlashcards(userId: String, subjectId: String): List<DueFlashcard> {
        return try {
            val response = api.getDueFlashcards(userId, subjectId)
            if (response.isSuccessful) {
                response.body()
                    ?.getAsJsonArray("flashcards")
                    ?.mapNotNull { el ->
                        val obj = el.asJsonObject
                        val id    = obj.get("id")?.asString ?: return@mapNotNull null
                        val front = obj.get("front")?.asString ?: return@mapNotNull null
                        val back  = obj.get("back")?.asString ?: return@mapNotNull null
                        DueFlashcard(id, front, back)
                    } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    private suspend fun getPlanoraUserId(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            firestore.collection("users").document(uid).get().await()
                .getString("planoraUserId") ?: uid
        } catch (e: Exception) { uid }
    }
}