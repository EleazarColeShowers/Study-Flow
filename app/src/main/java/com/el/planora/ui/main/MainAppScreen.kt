package com.el.planora.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.el.planora.ui.calendar.CalendarScreen
import com.el.planora.ui.checkin.CheckInScreen
import com.el.planora.ui.component.BottomNavBar
import com.el.planora.ui.component.BottomNavTab
import com.el.planora.ui.dashboard.DashboardScreen
import com.el.planora.ui.techniques.TechniquesScreen
import com.el.planora.ui.timer.TimerScreen

// Approximate height of the bottom nav bar including system nav inset
private val BOTTOM_NAV_HEIGHT = 80.dp

@Composable
fun MainAppScreen(
    onNavigateToProfile: () -> Unit,
    viewModel: MainAppViewModel = hiltViewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        // Content is padded at bottom so the nav bar never covers it
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = BOTTOM_NAV_HEIGHT)
        ) {
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
        }

        // Bottom nav bar — always visible, content padded above it
        BottomNavBar(
            currentTab = currentTab,
            onTabSelected = viewModel::selectTab,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}