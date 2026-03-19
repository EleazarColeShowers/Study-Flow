package com.el.planora.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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

@Composable
fun MainAppScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSubject: (String, String, String, Int) -> Unit = { _, _, _, _ -> },
    viewModel: MainAppViewModel = hiltViewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

    // Measure the actual nav bar height dynamically so content padding is exact
    val density = LocalDensity.current
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Bottom nav bar itself is ~56dp + system nav bar padding
    val bottomNavBarHeight = 60.dp+navBarHeight

    Box(modifier = Modifier.fillMaxSize()) {

        // Content padded by exact bottom nav bar height — no gap, no overlap
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomNavBarHeight)
        ) {
            when (currentTab) {
                BottomNavTab.DASHBOARD -> DashboardScreen(
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSubject = onNavigateToSubject
                )
                BottomNavTab.TECHNIQUES -> TechniquesScreen()
                BottomNavTab.CALENDAR   -> CalendarScreen()
                BottomNavTab.CHECK_IN   -> CheckInScreen(
                    onDismiss = { viewModel.selectTab(BottomNavTab.DASHBOARD) }
                )
                BottomNavTab.TIMER      -> TimerScreen(
                    onDismiss = { viewModel.selectTab(BottomNavTab.DASHBOARD) }
                )
            }
        }

        // Bottom nav bar sits at the very bottom, handles its own system inset
        BottomNavBar(
            currentTab = currentTab,
            onTabSelected = viewModel::selectTab,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}