package com.el.planora.data.mapper

import com.el.planora.domain.model.UserProfile

/**
 * Maps raw onboarding answer keys/values (from HomeViewModel.OnboardingState.answers)
 * to the structured UserProfile with exact API field values.
 *
 * Onboarding answer keys come from the Question.id values defined in HomeScreen.kt.
 */
object UserProfileMapper {

    fun fromOnboardingAnswers(
        uid: String,
        userName: String,
        email: String,
        answers: Map<String, Any>
    ): UserProfile {

        // ── Helper to get string answer safely ────────────────────────────────
        fun str(key: String) = answers[key] as? String ?: ""

        // ── Helper to get multi-select answer safely ───────────────────────────
        @Suppress("UNCHECKED_CAST")
        fun multi(key: String): Set<String> =
            (answers[key] as? Set<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()

        // ── user_category ──────────────────────────────────────────────────────
        val userCategory = when (str("user_type")) {
            "Uni Student"              -> "uni_student"
            "Language Learner"         -> "language_learner"
            "Certification Candidate"  -> "cert_candidate"
            "Studying a specific topic"-> "self_study"
            else                       -> "uni_student"
        }

        // ── Learning differences → has_* flags ────────────────────────────────
        val diffs = multi("learning_diffs")
        val hasAdhd     = if ("Hard to focus or sit still" in diffs) 1 else 0
        val hasDyslexia = if ("Struggle with reading or spelling" in diffs) 1 else 0
        val hasAutism   = if ("Prefer clear structure; changes stress me" in diffs) 1 else 0
        val hasAnxiety  = if ("Often anxious or overwhelmed studying" in diffs) 1 else 0

        // ── attention_span ─────────────────────────────────────────────────────
        // Key exists in uni (uni_attention), lang (lang_attention), cert (cert_attention)
        val attentionRaw = str("uni_attention")
            .ifEmpty { str("lang_attention") }
            .ifEmpty { str("cert_attention") }

        val attentionSpan = when (attentionRaw) {
            "Under 10 mins" -> "under_10"
            "10–20 mins"    -> "10_20"
            "20–45 mins"    -> "20_45"
            "45+ mins"      -> "45_plus"
            else            -> "20_45"
        }

        // ── sleep_hours ────────────────────────────────────────────────────────
        val sleepHours = when (str("sleep")) {
            "Less than 5 hrs" -> 4.5f
            "5–6 hrs"         -> 5.5f
            "7–8 hrs"         -> 7.5f
            "8+ hrs"          -> 8.5f
            else              -> 7.0f
        }

        // ── daily_study_hrs ────────────────────────────────────────────────────
        val dailyStudyHrs = when (str("hours_per_day")) {
            "Less than 1 hour" -> 0.5f
            "1–2 hours"        -> 1.5f
            "2–3 hours"        -> 2.5f
            "3+ hours"         -> 3.5f
            else               -> 2.0f
        }

        // ── learning_style ─────────────────────────────────────────────────────
        val learningStyleRaw = str("uni_learning_style")
            .ifEmpty { str("cert_learning_style") }

        val learningStyle = when {
            "read it carefully" in learningStyleRaw    -> "read_write"
            "watched it explained" in learningStyleRaw -> "visual"
            "heard it discussed" in learningStyleRaw   -> "auditory"
            "tried it" in learningStyleRaw             -> "kinesthetic"
            else                                       -> "read_write"
        }

        // ── peak_focus_time ────────────────────────────────────────────────────
        val peakFocusTime = when (str("best_time")) {
            "Morning"    -> "morning"
            "Afternoon"  -> "afternoon"
            "Evening"    -> "evening"
            "Late Night" -> "late_night"
            else         -> "morning"
        }

        // ── study_env ──────────────────────────────────────────────────────────
        val studyEnv = when (str("study_location")) {
            "Home – quiet & dedicated"     -> "quiet_home"
            "Home – noisy or distracting"  -> "noisy_home"
            "Library or campus"            -> "library"
            "Cafés or public spaces"       -> "cafe"
            else                           -> "varies"
        }

        // ── struggle ───────────────────────────────────────────────────────────
        val struggles = multi("uni_struggles")
            .ifEmpty { multi("self_challenges") }

        val struggle = when {
            "Staying focused" in struggles            -> "staying_focused"
            "Remembering information" in struggles    -> "remembering"
            "Understanding concepts" in struggles     -> "understanding"
            "Managing time" in struggles              -> "managing_time"
            "Motivation" in struggles                 -> "motivation"
            "I know what to study but not how" in struggles -> "dont_know_how_to_study"
            // cert-specific
            "Running out of time" in multi("cert_concerns")        -> "running_out_of_time"
            "Covering all the content" in multi("cert_concerns")   -> "covering_all_content"
            "Exam technique & timing" in multi("cert_concerns")    -> "exam_technique"
            "Staying consistent" in multi("cert_concerns")         -> "staying_consistent"
            else                                      -> "understanding"
        }

        // ── current_level (language learner only) ─────────────────────────────
        val currentLevel = when (str("lang_level")) {
            "Complete beginner" -> "beginner"
            "Basic understanding" -> "basic"
            "Conversational"    -> "conversational"
            "Advanced"          -> "advanced"
            "Near fluent"       -> "near_fluent"
            else                -> "not_applicable"
        }

        // ── prior_attempt (cert only) ──────────────────────────────────────────
        val priorAttempt = when (str("cert_attempts")) {
            "No – first attempt"             -> "first_time"
            "Yes – didn't pass, retrying"    -> "retaking"
            "Yes – passed, recertifying"     -> "recertifying"
            else                             -> "not_applicable"
        }

        // ── success_goal ───────────────────────────────────────────────────────
        val successGoal = str("success_goal")

        // ── raw answers (flatten sets to comma-separated strings for Firestore)─
        val rawAnswers = answers.mapValues { (_, v) ->
            when (v) {
                is Set<*> -> v.filterIsInstance<String>().joinToString(", ")
                else      -> v.toString()
            }
        }

        return UserProfile(
            uid           = uid,
            userName      = userName,
            email         = email,
            userCategory  = userCategory,
            hasAdhd       = hasAdhd,
            hasDyslexia   = hasDyslexia,
            hasAutism     = hasAutism,
            hasAnxiety    = hasAnxiety,
            attentionSpan = attentionSpan,
            sleepHours    = sleepHours,
            dailyStudyHrs = dailyStudyHrs,
            learningStyle = learningStyle,
            peakFocusTime = peakFocusTime,
            studyEnv      = studyEnv,
            struggle      = struggle,
            currentLevel  = currentLevel,
            priorAttempt  = priorAttempt,
            successGoal   = successGoal,
            rawAnswers    = rawAnswers
        )
    }

    /**
     * Converts UserProfile back to the Map the Planora API expects
     * for user_profile fields in /checkin/start, /recommend etc.
     */
    fun toApiMap(profile: UserProfile): Map<String, Any> = mapOf(
        "user_name"       to profile.userName,
        "user_category"   to profile.userCategory,
        "has_adhd"        to profile.hasAdhd,
        "has_dyslexia"    to profile.hasDyslexia,
        "has_autism"      to profile.hasAutism,
        "has_anxiety"     to profile.hasAnxiety,
        "attention_span"  to profile.attentionSpan,
        "sleep_hours"     to profile.sleepHours,
        "daily_study_hrs" to profile.dailyStudyHrs,
        "learning_style"  to profile.learningStyle,
        "peak_focus_time" to profile.peakFocusTime,
        "study_env"       to profile.studyEnv,
        "struggle"        to profile.struggle,
        "current_level"   to profile.currentLevel,
        "prior_attempt"   to profile.priorAttempt,
        "success_goal"    to profile.successGoal
    )
}