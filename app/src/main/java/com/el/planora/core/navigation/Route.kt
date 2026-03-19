package com.el.planora.core.navigation

sealed class Route(val path: String) {

    // ── Auth Flow ──────────────────────────────────────────────────────────────
    data object Splash : Route("splash")
    data object Onboarding : Route("onboarding")

    sealed class Auth(path: String) : Route("auth/$path") {
        data object Login : Auth("login")
        data object SignUp : Auth("signup")
        data object ForgotPassword : Auth("forgot-password")
    }

    // ── Main App Flow ──────────────────────────────────────────────────────────
    data object Home : Route("home")
    data object Main : Route("main")


    sealed class Study(path: String) : Route("study/$path") {
        data object Dashboard : Study("dashboard")

        // Navigates to a specific subject by ID
        object SubjectDetail : Route("study/subject/{subjectId}/{subjectName}/{contentType}/{daysToExam}") {
            const val ARG_SUBJECT_ID    = "subjectId"
            const val ARG_SUBJECT_NAME  = "subjectName"
            const val ARG_CONTENT_TYPE  = "contentType"
            const val ARG_DAYS_TO_EXAM  = "daysToExam"

            fun createRoute(
                subjectId: String,
                subjectName: String,
                contentType: String,
                daysToExam: Int
            ) = "study/subject/$subjectId/${subjectName.encode()}/${contentType.encode()}/$daysToExam"

            private fun String.encode() =
                java.net.URLEncoder.encode(this, "UTF-8")
        }


        data object FlashCards : Study("flashcards/{deckId}") {
            fun createRoute(deckId: String) = "study/flashcards/$deckId"
            const val ARG_DECK_ID = "deckId"
        }

        data object Quiz : Study("quiz/{quizId}") {
            fun createRoute(quizId: String) = "study/quiz/$quizId"
            const val ARG_QUIZ_ID = "quizId"
        }

        data object Results : Study("results/{sessionId}") {
            fun createRoute(sessionId: String) = "study/results/$sessionId"
            const val ARG_SESSION_ID = "sessionId"
        }
    }

    // ── AI Tutor ───────────────────────────────────────────────────────────────
    sealed class AI(path: String) : Route("ai/$path") {
        data object Chat : AI("chat")
        data object Summarize : AI("summarize")
        data object ReadAloud : AI("read-aloud")
    }

    // ── Profile ────────────────────────────────────────────────────────────────
    sealed class Profile(path: String) : Route("profile/$path") {
        data object Main : Profile("main")
        data object Settings : Profile("settings")
        data object Accessibility : Profile("accessibility")
        data object Progress : Profile("progress")
    }
}