package com.el.planora.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.remote.planoraApiService
import com.el.planora.data.repository.ApiResult
import com.el.planora.data.repository.UserProfileRepository
import com.el.planora.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = true,
    val username: String = "",
    val email: String = "",

    // From Firestore onboarding answers
    val studySubject: String = "",
    val studyGoal: String = "",
    val dailyDedication: String = "",
    val studyTime: String = "",
    val attentionSpan: String = "",
    val learningDifferences: List<String> = emptyList(),
    val userCategory: String = "",

    // From Planora API subjects
    val activeSubjects: List<String> = emptyList(),

    val isLoggingOut: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userProfileRepository: UserProfileRepository,
    private val api: planoraApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                _state.update { it.copy(isLoading = false, error = "Not logged in") }
                return@launch
            }

            // ── 1. Firebase Auth — name + email ───────────────────────────────
            val email    = firebaseUser.email ?: ""
            val authName = firebaseUser.displayName ?: ""

            // ── 2. Firestore — onboarding profile ─────────────────────────────
            val firestoreProfile = try {
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()
            } catch (e: Exception) { null }

            val username    = firestoreProfile?.getString("userName") ?: authName.ifBlank { email.substringBefore("@") }
            val userCategory = firestoreProfile?.getString("userCategory") ?: ""

            // Map raw stored values back to readable labels
            val studyTime     = mapPeakFocusTime(firestoreProfile?.getString("peakFocusTime"))
            val attentionSpan = mapAttentionSpan(firestoreProfile?.getString("attentionSpan"))
            val struggle      = mapStruggle(firestoreProfile?.getString("struggle"))
            val successGoal   = firestoreProfile?.getString("successGoal") ?: ""

            // Learning differences from rawAnswers
            val rawLearningDiffs = firestoreProfile?.getString("rawAnswers.learning_diffs") ?: ""
            val learningDiffs = parseLearningDifferences(
                firestoreProfile?.get("rawAnswers") as? Map<*, *>
            )

            // Daily dedication from rawAnswers
            val dailyDedication = (firestoreProfile?.get("rawAnswers") as? Map<*, *>)
                ?.get("hours_per_day")?.toString() ?: ""

            // Subject from rawAnswers based on category
            val rawAnswers = firestoreProfile?.get("rawAnswers") as? Map<*, *>
            val studySubject = when (userCategory) {
                "uni_student"      -> rawAnswers?.get("uni_subjects")?.toString()
                    ?.split(",")?.firstOrNull()?.trim() ?: ""
                "language_learner" -> rawAnswers?.get("lang_language")?.toString() ?: ""
                "cert_candidate"   -> rawAnswers?.get("cert_name")?.toString() ?: ""
                "self_study"       -> rawAnswers?.get("self_topic")?.toString() ?: ""
                else               -> rawAnswers?.get("uni_subjects")?.toString()
                    ?.split(",")?.firstOrNull()?.trim() ?: ""
            }

            // ── 3. Planora API — active subjects ──────────────────────────────
            val planoraUserId = firestoreProfile?.getString("planoraUserId")
                ?: firebaseUser.uid
            val activeSubjects = try {
                val response = api.getSubjects(planoraUserId)
                if (response.isSuccessful) {
                    response.body()
                        ?.getAsJsonArray("subjects")
                        ?.mapNotNull { it.asJsonObject.get("subject_name")?.asString }
                        ?: emptyList()
                } else emptyList()
            } catch (e: Exception) { emptyList() }

            // ── Update state with everything ──────────────────────────────────
            _state.update {
                it.copy(
                    isLoading          = false,
                    username           = username,
                    email              = email,
                    userCategory       = mapUserCategory(userCategory),
                    studySubject       = studySubject,
                    studyGoal          = successGoal,
                    dailyDedication    = dailyDedication,
                    studyTime          = studyTime,
                    attentionSpan      = attentionSpan,
                    learningDifferences = learningDiffs,
                    activeSubjects     = activeSubjects
                )
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoggingOut = true) }
            auth.signOut()
            onLoggedOut()
        }
    }

    // ── Mappers — convert API/Firestore values to readable labels ─────────────

    private fun mapPeakFocusTime(value: String?) = when (value) {
        "morning"   -> "Morning"
        "afternoon" -> "Afternoon"
        "evening"   -> "Evening"
        "late_night"-> "Late Night"
        else        -> value ?: ""
    }

    private fun mapAttentionSpan(value: String?) = when (value) {
        "under_10" -> "Under 10 minutes"
        "10_20"    -> "10–20 minutes"
        "20_45"    -> "20–45 minutes"
        "45_plus"  -> "45+ minutes"
        else       -> value ?: ""
    }

    private fun mapStruggle(value: String?) = when (value) {
        "staying_focused"        -> "Staying focused"
        "remembering"            -> "Remembering information"
        "understanding"          -> "Understanding concepts"
        "managing_time"          -> "Managing time"
        "motivation"             -> "Motivation"
        "dont_know_how_to_study" -> "Don't know how to study"
        else                     -> value ?: ""
    }

    private fun mapUserCategory(value: String?) = when (value) {
        "uni_student"      -> "University Student"
        "language_learner" -> "Language Learner"
        "cert_candidate"   -> "Certification Candidate"
        "self_study"       -> "Self Study"
        else               -> value ?: ""
    }

    private fun parseLearningDifferences(rawAnswers: Map<*, *>?): List<String> {
        val raw = rawAnswers?.get("learning_diffs")?.toString() ?: return emptyList()
        return raw.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "None of these" && it != "I'd rather not say" }
    }
}