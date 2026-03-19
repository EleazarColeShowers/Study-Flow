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

enum class ChatMode { QA, CHECKIN }

enum class CheckInStage { IDLE, PINGING, CHECKIN, MATERIAL_PROMPT, QUIZ, COMPLETE, ERROR }

data class CheckInUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val mode: ChatMode = ChatMode.CHECKIN,
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
        // Load profile first, then auto-start check-in in sequence
        loadProfileThenStartCheckIn()
    }

    // ── Step 1: Load profile, then immediately start check-in ─────────────────
    // Everything runs sequentially in one coroutine — no race condition possible
    private fun loadProfileThenStartCheckIn() {
        viewModelScope.launch {

            // ── Load profile ──────────────────────────────────────────────────
            val subjectName: String
            val userName: String

            when (val result = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> {
                    val profile = result.data
                    subjectName = resolveSubject(profile.rawAnswers, profile.userCategory)
                    userName = profile.userName
                    _state.update {
                        it.copy(subjectName = subjectName, isLoadingProfile = false)
                    }
                }
                else -> {
                    subjectName = "your subject"
                    userName = ""
                    _state.update { it.copy(isLoadingProfile = false, subjectName = subjectName) }
                }
            }

            // ── Show greeting ─────────────────────────────────────────────────
            val greeting = if (userName.isNotEmpty()) {
                "Hi $userName 👋 I'm your Planora study coach. Starting your check-in for **$subjectName**..."
            } else {
                "Hi! 👋 I'm your Planora study coach. Starting your check-in..."
            }
            addBotMessage(greeting)

            // ── Ping server ───────────────────────────────────────────────────
            _state.update { it.copy(checkInStage = CheckInStage.PINGING) }
            addBotMessage("Connecting to Planora... ⏳")

            when (checkInRepository.ping()) {
                is ApiResult.Success -> {
                    // ── Start check-in session ─────────────────────────────────
                    val userId = UUID.randomUUID().toString()
                    _state.update { it.copy(isBotTyping = true) }

                    when (val result = checkInRepository.startCheckIn(
                        userId = userId,
                        subjectName = subjectName.ifEmpty { "Today's Subject" }
                    )) {
                        is ApiResult.Success -> {
                            // Store userId BEFORE updating stage so sendMessage() can read it
                            _state.update {
                                it.copy(
                                    sessionUserId = userId,
                                    checkInStage  = CheckInStage.CHECKIN,
                                    isBotTyping   = false
                                )
                            }
                            addBotMessage(result.data.message)
                        }
                        is ApiResult.Error -> {
                            _state.update {
                                it.copy(
                                    checkInStage = CheckInStage.ERROR,
                                    isBotTyping  = false
                                )
                            }
                            addBotMessage("⚠️ ${result.message}\n\nYou can still use Q&A mode.")
                        }
                        else -> Unit
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(checkInStage = CheckInStage.ERROR) }
                    addBotMessage("⚠️ Couldn't reach the server. Tap **Check In** to retry, or use Q&A mode.")
                }
                else -> Unit
            }
        }
    }

    // ── Input update ──────────────────────────────────────────────────────────
    fun updateInput(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    // ── Send message ──────────────────────────────────────────────────────────
    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank() || _state.value.isBotTyping) return

        // Guard: don't send check-in messages if session not established
        if (_state.value.mode == ChatMode.CHECKIN &&
            _state.value.sessionUserId.isEmpty()) {
            addBotMessage("⚠️ Session not ready yet. Please wait...")
            return
        }

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

    // ── Check-in mode ─────────────────────────────────────────────────────────
    private fun handleCheckInMessage(message: String) {
        viewModelScope.launch {
            val userId = _state.value.sessionUserId
            when (val result = checkInRepository.sendMessage(userId, message)) {
                is ApiResult.Success -> {
                    val response = result.data
                    val newStage = when (response.stage) {
                        "material_prompt" -> CheckInStage.MATERIAL_PROMPT
                        "quiz"            -> CheckInStage.QUIZ
                        "complete"        -> CheckInStage.COMPLETE
                        else              -> CheckInStage.CHECKIN
                    }
                    _state.update {
                        it.copy(
                            isBotTyping  = false,
                            checkInStage = newStage,
                            summary      = response.summary
                        )
                    }
                    addBotMessage(response.message)

                    if (newStage == CheckInStage.COMPLETE) {
                        _state.update { it.copy(mode = ChatMode.QA) }
                        addBotMessage("✅ Session logged! Tap Q&A to ask me anything.")
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isBotTyping = false) }
                    if (result.message == "SESSION_EXPIRED") {
                        addBotMessage("Session timed out. Tap **Check In** to start a new one.")
                        _state.update { it.copy(checkInStage = CheckInStage.IDLE, sessionUserId = "") }
                    } else {
                        addBotMessage("⚠️ ${result.message}")
                    }
                }
                else -> Unit
            }
        }
    }

    // ── Mode switching ────────────────────────────────────────────────────────

    fun switchToQA() {
        if (_state.value.mode == ChatMode.QA) return
        _state.update { it.copy(mode = ChatMode.QA) }
        addBotMessage("Switched to Q&A mode 💬 Ask me anything about ${_state.value.subjectName.ifEmpty { "your subject" }}.")
    }

    // Called from QA mode to start a fresh check-in
    fun startCheckIn() {
        if (_state.value.mode == ChatMode.CHECKIN) return
        _state.update {
            it.copy(
                mode          = ChatMode.CHECKIN,
                checkInStage  = CheckInStage.IDLE,
                sessionUserId = "",
                summary       = null
            )
        }
        retryCheckIn()
    }

    // Called when check-in errored and user wants to retry
    fun retryCheckIn() {
        viewModelScope.launch {
            _state.update { it.copy(checkInStage = CheckInStage.PINGING) }
            addBotMessage("Connecting to Planora... ⏳")

            when (checkInRepository.ping()) {
                is ApiResult.Success -> {
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
                                    checkInStage  = CheckInStage.CHECKIN,
                                    isBotTyping   = false
                                )
                            }
                            addBotMessage(result.data.message)
                        }
                        is ApiResult.Error -> {
                            _state.update {
                                it.copy(checkInStage = CheckInStage.ERROR, isBotTyping = false)
                            }
                            addBotMessage("⚠️ ${result.message}")
                        }
                        else -> Unit
                    }
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(checkInStage = CheckInStage.ERROR) }
                    addBotMessage("⚠️ Couldn't reach the server. Check your connection.")
                }
                else -> Unit
            }
        }
    }

    fun onDismiss() {
        val userId = _state.value.sessionUserId
        if (userId.isNotEmpty() && _state.value.mode == ChatMode.CHECKIN) {
            viewModelScope.launch { checkInRepository.endCheckIn(userId) }
        }
    }

    fun reset() { _state.value = CheckInUiState() }

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