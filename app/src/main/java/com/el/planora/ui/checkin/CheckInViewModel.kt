package com.el.planora.ui.checkin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.remote.model.CheckInSummaryDto
import com.el.planora.data.repository.ApiResult
import com.el.planora.data.repository.CheckInRepository
import com.el.planora.data.repository.QaRepository
import com.el.planora.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

// ── Models ────────────────────────────────────────────────────────────────────

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
)

enum class ChatMode {
    QA,        // Default — user asks study questions
    CHECKIN    // Check-in flow — multi-turn conversation
}

enum class CheckInStage { IDLE, PINGING, CHECKIN,MATERIAL_PROMPT, QUIZ, COMPLETE, ERROR }

data class CheckInUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val mode: ChatMode = ChatMode.QA,
    val checkInStage: CheckInStage = CheckInStage.IDLE,
    val isBotTyping: Boolean = false,
    val summary: CheckInSummaryDto? = null,
    val errorMessage: String? = null,
    val sessionUserId: String = "",
    val subjectName: String = "",
    val isLoadingProfile: Boolean = true
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class CheckInViewModel @Inject constructor(
    private val qaRepository: QaRepository,
    private val checkInRepository: CheckInRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CheckInUiState())
    val state: StateFlow<CheckInUiState> = _state.asStateFlow()

    init {
        loadProfileAndGreet()
    }

    // ── Load profile on open, show greeting ───────────────────────────────────
    private fun loadProfileAndGreet() {
        viewModelScope.launch {
            when (val result = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> {
                    val profile = result.data
                    val subject = resolveSubject(profile.rawAnswers, profile.userCategory)
                    _state.update {
                        it.copy(
                            subjectName = subject,
                            isLoadingProfile = false
                        )
                    }
                    addBotMessage(
                        "Hi ${profile.userName} 👋 I'm your Planora study coach.\n\n" +
                                "You can ask me anything about **$subject**, or tap " +
                                "**Check In** to log today's session."
                    )
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isLoadingProfile = false, subjectName = "your subject") }
                    addBotMessage(
                        "Hi! 👋 I'm your Planora study coach.\n\n" +
                                "Ask me any study question, or tap **Check In** to log today's session."
                    )
                }
                else -> Unit
            }
        }
    }

    // ── Input update ──────────────────────────────────────────────────────────
    fun updateInput(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    // ── Send message — routes to QA or Check-in based on mode ─────────────────
    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isBotTyping) return

        _state.update {
            it.copy(
                messages = it.messages + ChatMessage(text = text, isUser = true),
                inputText = "",
                isBotTyping = true
            )
        }

        when (_state.value.mode) {
            ChatMode.QA      -> handleQaMessage(text)
            ChatMode.CHECKIN -> handleCheckInMessage(text)
        }
    }

    // ── Q&A mode ──────────────────────────────────────────────────────────────
    private fun handleQaMessage(question: String) {
        viewModelScope.launch {
            // Show typing indicator while potentially waking server
            when (val pingResult = checkInRepository.ping()) {
                is ApiResult.Error -> {
                    _state.update { it.copy(isBotTyping = false) }
                    addBotMessage("⚠️ Couldn't reach the server. Check your connection.")
                    return@launch
                }
                else -> Unit
            }

            when (val result = qaRepository.askQuestion(question)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isBotTyping = false) }
                    val answer = result.data.answer
                    if (!answer.isNullOrBlank()) {
                        addBotMessage(answer)
                    } else {
                        addBotMessage("⚠️ No answer returned. The backend may still be warming up — try again.")
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isBotTyping = false) }
                    addBotMessage("⚠️ ${result.message}")
                }
                else -> Unit
            }
        }
    }

    // ── Check-in mode — sends to /checkin/message ─────────────────────────────
    private fun handleCheckInMessage(message: String) {
        viewModelScope.launch {
            val userId = _state.value.sessionUserId
            when (val result = checkInRepository.sendMessage(userId, message)) {
                is ApiResult.Success -> {
                    val response = result.data
                    val newStage = when (response.stage) {
                        "quiz"     -> CheckInStage.QUIZ
                        "complete" -> CheckInStage.COMPLETE
                        "material_prompt"  -> CheckInStage.MATERIAL_PROMPT
                        else       -> CheckInStage.CHECKIN
                    }
                    _state.update {
                        it.copy(
                            isBotTyping = false,
                            checkInStage = newStage,
                            summary = response.summary
                        )
                    }
                    addBotMessage(response.message)

                    // After complete, switch back to QA mode
                    if (newStage == CheckInStage.COMPLETE) {
                        _state.update { it.copy(mode = ChatMode.QA) }
                        addBotMessage(
                            "✅ Session logged! You can keep asking me study questions."
                        )
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isBotTyping = false) }
                    if (result.message == "SESSION_EXPIRED") {
                        addBotMessage("Session timed out. Tap **Check In** to start again.")
                        _state.update {
                            it.copy(mode = ChatMode.QA, checkInStage = CheckInStage.IDLE)
                        }
                    } else {
                        addBotMessage("⚠️ ${result.message}")
                    }
                }
                else -> Unit
            }
        }
    }

    // ── Check-in button tapped ────────────────────────────────────────────────
    fun startCheckIn() {
        if (_state.value.mode == ChatMode.CHECKIN) return
        pingThenStartCheckIn()
    }

    private fun pingThenStartCheckIn() {
        viewModelScope.launch {
            _state.update { it.copy(mode = ChatMode.CHECKIN, checkInStage = CheckInStage.PINGING) }
            addBotMessage("Connecting to Planora... ⏳")

            when (checkInRepository.ping()) {
                is ApiResult.Success -> beginCheckIn()
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(mode = ChatMode.QA, checkInStage = CheckInStage.IDLE)
                    }
                    addBotMessage("⚠️ Couldn't reach the server. Check your connection and try again.")
                }
                else -> Unit
            }
        }
    }

    private suspend fun beginCheckIn() {
        val userId = UUID.randomUUID().toString()
        _state.update { it.copy(isBotTyping = true) }

        when (val result = checkInRepository.startCheckIn(
            userId = userId,
            subjectName = _state.value.subjectName.ifEmpty { "Today's Subject" }
        )) {
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        sessionUserId = userId,
                        checkInStage = CheckInStage.CHECKIN,
                        isBotTyping = false
                    )
                }
                addBotMessage(result.data.message)
            }
            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        mode = ChatMode.QA,
                        checkInStage = CheckInStage.IDLE,
                        isBotTyping = false
                    )
                }
                addBotMessage("⚠️ ${result.message}")
            }
            else -> Unit
        }
    }

    // ── Dismiss — clean up check-in session if open ───────────────────────────
    fun onDismiss() {
        val userId = _state.value.sessionUserId
        if (userId.isNotEmpty() && _state.value.mode == ChatMode.CHECKIN) {
            viewModelScope.launch { checkInRepository.endCheckIn(userId) }
        }
    }

    fun reset() { _state.value = CheckInUiState() }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun addBotMessage(text: String) {
        _state.update {
            it.copy(messages = it.messages + ChatMessage(text = text, isUser = false))
        }
    }

    private fun resolveSubject(rawAnswers: Map<String, String>, userCategory: String): String =
        when (userCategory) {
            "uni_student"      -> rawAnswers["uni_subjects"]
                ?.split(",")?.firstOrNull()?.trim() ?: "your subject"
            "language_learner" -> rawAnswers["lang_language"] ?: "your language"
            "cert_candidate"   -> rawAnswers["cert_name"] ?: "your certification"
            "self_study"       -> rawAnswers["self_topic"] ?: "your topic"
            else               -> rawAnswers["uni_subjects"]
                ?.split(",")?.firstOrNull()?.trim() ?: "your subject"
        }
}