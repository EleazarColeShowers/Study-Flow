package com.el.studyflow.core.navigation

import android.R.attr.type
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.el.studyflow.ui.auth.LoginScreen
import com.el.studyflow.ui.auth.OnboardingScreen
import com.el.studyflow.ui.auth.SignUpScreen
import com.el.studyflow.ui.auth.SplashScreen
import com.el.studyflow.ui.home.HomeScreen
import com.el.studyflow.ui.main.MainAppScreen
import com.el.studyflow.ui.profile.ProfileScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Route.Splash.path,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ── Splash ─────────────────────────────────────────────────────────────
        composable(route = Route.Splash.path) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Route.Onboarding.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ─────────────────────────────────────────────────────────
        composable(route = Route.Onboarding.path) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigate(Route.Auth.Login.path) // no popUpTo - keep onboarding in stack
                },
                onNavigateToRegister = {
                    navController.navigate(Route.Auth.SignUp.path) // no popUpTo - keep onboarding in stack
                }
            )
        }

        composable(route = Route.Auth.Login.path) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(0) { inclusive = true } // clear entire back stack on success
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Route.Auth.SignUp.path) {
                        popUpTo(Route.Auth.Login.path) { inclusive = true } // replace login with signup, back goes to onboarding
                    }
                }
            )
        }

        composable(route = Route.Auth.SignUp.path) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Route.Home.path) {
                        popUpTo(0) { inclusive = true } // clear entire back stack on success
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Route.Auth.Login.path) {
                        popUpTo(Route.Auth.SignUp.path) { inclusive = true } // replace signup with login, back goes to onboarding
                    }
                }
            )
        }

        // ── Auth: Forgot Password ──────────────────────────────────────────────
        composable(route = Route.Auth.ForgotPassword.path) {
            // ForgotPasswordScreen - to be built
        }

        // ── Home (Bottom Nav Host) ─────────────────────────────────────────────
        composable(route = Route.Home.path) {
             HomeScreen(
                 onSetupComplete = {
                     navController.navigate(Route.Main.path) {
                         popUpTo(0) { inclusive = true } // clear entire back stack on success
                     }
                 }
             )
        }

        composable(route = Route.Main.path) {
            MainAppScreen(
                onNavigateToProfile = {
                    navController.navigate(Route.Profile.Main.path)
                }
            )
        }

        // ── Profile ────────────────────────────────────────────────────────────
        composable(route = Route.Profile.Main.path) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoggedOut = {
                    navController.navigate(Route.Auth.Login.path) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Study: Subject Detail ──────────────────────────────────────────────
        composable(
            route = Route.Study.SubjectDetail.path,
            arguments = listOf(
                navArgument(Route.Study.SubjectDetail.ARG_SUBJECT_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString(
                Route.Study.SubjectDetail.ARG_SUBJECT_ID
            ) ?: return@composable
            // SubjectDetailScreen(subjectId = subjectId)
        }

        // ── Study: Flashcards ──────────────────────────────────────────────────
        composable(
            route = Route.Study.FlashCards.path,
            arguments = listOf(
                navArgument(Route.Study.FlashCards.ARG_DECK_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString(
                Route.Study.FlashCards.ARG_DECK_ID
            ) ?: return@composable
            // FlashCardsScreen(deckId = deckId)
        }

        // ── Study: Quiz ────────────────────────────────────────────────────────
        composable(
            route = Route.Study.Quiz.path,
            arguments = listOf(
                navArgument(Route.Study.Quiz.ARG_QUIZ_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString(
                Route.Study.Quiz.ARG_QUIZ_ID
            ) ?: return@composable
            // QuizScreen(quizId = quizId)
        }

        // ── AI: Chat ───────────────────────────────────────────────────────────
        composable(route = Route.AI.Chat.path) {
            // AIChatScreen()
        }


        composable(route = Route.Profile.Accessibility.path) {
            // AccessibilityScreen() - dyslexia fonts, color filters, TTS settings
        }
    }
}