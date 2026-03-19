package com.el.planora.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.el.planora.ui.auth.LoginScreen
import com.el.planora.ui.auth.OnboardingScreen
import com.el.planora.ui.auth.SignUpScreen
import com.el.planora.ui.auth.SplashScreen
import com.el.planora.ui.home.HomeScreen
import com.el.planora.ui.main.MainAppScreen
import com.el.planora.ui.profile.ProfileScreen
import com.el.planora.ui.subject.SubjectDetailScreen

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

        composable(route = Route.Splash.path) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Route.Onboarding.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Route.Main.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Route.Onboarding.path) {
            OnboardingScreen(
                onNavigateToLogin = { navController.navigate(Route.Auth.Login.path) },
                onNavigateToRegister = { navController.navigate(Route.Auth.SignUp.path) }
            )
        }

        composable(route = Route.Auth.Login.path) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Main.path) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToSignUp = {
                    navController.navigate(Route.Auth.SignUp.path) {
                        popUpTo(Route.Auth.Login.path) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Route.Auth.SignUp.path) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Route.Home.path) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToLogin = {
                    navController.navigate(Route.Auth.Login.path) {
                        popUpTo(Route.Auth.SignUp.path) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Route.Auth.ForgotPassword.path) { }

        composable(route = Route.Home.path) {
            HomeScreen(
                onSetupComplete = {
                    navController.navigate(Route.Main.path) { popUpTo(0) { inclusive = true } }
                }
            )
        }

        // ── Main — passes subject navigation down ──────────────────────────────
        composable(route = Route.Main.path) {
            MainAppScreen(
                onNavigateToProfile = {
                    navController.navigate(Route.Profile.Main.path)
                },
                onNavigateToSubject = { subjectId, subjectName, contentType, daysToExam ->
                    navController.navigate(
                        Route.Study.SubjectDetail.createRoute(
                            subjectId, subjectName, contentType, daysToExam
                        )
                    )
                }
            )
        }

        // ── Subject Detail — single registration only ──────────────────────────
        composable(
            route = Route.Study.SubjectDetail.path,
            arguments = listOf(
                navArgument(Route.Study.SubjectDetail.ARG_SUBJECT_ID)   { type = NavType.StringType },
                navArgument(Route.Study.SubjectDetail.ARG_SUBJECT_NAME) { type = NavType.StringType },
                navArgument(Route.Study.SubjectDetail.ARG_CONTENT_TYPE) { type = NavType.StringType },
                navArgument(Route.Study.SubjectDetail.ARG_DAYS_TO_EXAM) {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) {
            SubjectDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Route.Profile.Main.path) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Route.Auth.Login.path) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Route.Study.FlashCards.path,
            arguments = listOf(navArgument(Route.Study.FlashCards.ARG_DECK_ID) { type = NavType.StringType })
        ) { }

        composable(
            route = Route.Study.Quiz.path,
            arguments = listOf(navArgument(Route.Study.Quiz.ARG_QUIZ_ID) { type = NavType.StringType })
        ) { }

        composable(route = Route.AI.Chat.path) { }
        composable(route = Route.Profile.Accessibility.path) { }
    }
}