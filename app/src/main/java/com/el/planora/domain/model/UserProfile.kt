package com.el.planora.domain.model

/**
 * The structured user profile stored in Firestore and sent to the Planora API.
 * All field names and values match the API's accepted values exactly.
 */
data class UserProfile(
    val uid: String = "",
    val userName: String = "",
    val email: String = "",

    // ── Planora API fields ─────────────────────────────────────────────────────
    // user_category: uni_student | language_learner | cert_candidate | self_study
    val userCategory: String = "uni_student",

    // has_* flags: 0 or 1
    val hasAdhd: Int = 0,
    val hasDyslexia: Int = 0,
    val hasAutism: Int = 0,
    val hasAnxiety: Int = 0,

    // attention_span: under_10 | 10_20 | 20_45 | 45_plus
    val attentionSpan: String = "20_45",

    // sleep_hours: float
    val sleepHours: Float = 7.0f,

    // daily_study_hrs: float
    val dailyStudyHrs: Float = 2.0f,

    // learning_style: read_write | visual | auditory | kinesthetic
    val learningStyle: String = "read_write",

    // peak_focus_time: morning | afternoon | evening | late_night
    val peakFocusTime: String = "morning",

    // study_env: quiet_home | noisy_home | library | cafe | varies
    val studyEnv: String = "varies",

    // struggle: staying_focused | remembering | understanding | managing_time |
    //           motivation | dont_know_how_to_study (+ cert-specific values)
    val struggle: String = "understanding",

    // current_level: beginner | basic | conversational | advanced |
    //                near_fluent | not_applicable
    val currentLevel: String = "not_applicable",

    // prior_attempt: first_time | retaking | recertifying | not_applicable
    val priorAttempt: String = "not_applicable",

    // success_goal: free text
    val successGoal: String = "",

    // ── Raw onboarding answers (stored for reference) ──────────────────────────
    val rawAnswers: Map<String, String> = emptyMap()
)