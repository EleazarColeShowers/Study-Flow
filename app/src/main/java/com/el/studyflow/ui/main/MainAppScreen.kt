package com.el.studyflow.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.studyflow.ui.calendar.CalendarScreen
import com.el.studyflow.ui.checkin.CheckInScreen
import com.el.studyflow.ui.component.BottomNavBar
import com.el.studyflow.ui.component.BottomNavTab
import com.el.studyflow.ui.dashboard.DashboardScreen
import com.el.studyflow.ui.techniques.TechniquesScreen
import com.el.studyflow.ui.timer.TimerScreen

@Composable
fun MainAppScreen(
    onNavigateToProfile: () -> Unit,
    viewModel: MainAppViewModel = hiltViewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // Tab content
        when (currentTab) {
            BottomNavTab.DASHBOARD -> DashboardScreen(
                onNavigateToProfile = onNavigateToProfile
            )
            BottomNavTab.TECHNIQUES -> TechniquesScreen()
            BottomNavTab.CALENDAR -> CalendarScreen()
            BottomNavTab.CHECK_IN -> CheckInScreen(
                onDismiss = { viewModel.selectTab(BottomNavTab.DASHBOARD) }
            )
            BottomNavTab.TIMER -> TimerScreen(
                onDismiss = { viewModel.selectTab(BottomNavTab.DASHBOARD) }
            )
        }

        // Bottom navigation bar - positioned at bottom
        BottomNavBar(
            currentTab = currentTab,
            onTabSelected = viewModel::selectTab,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}