package com.el.studyflow.ui.main

import androidx.lifecycle.ViewModel
import com.el.studyflow.ui.component.BottomNavTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainAppViewModel @Inject constructor() : ViewModel() {

    private val _currentTab = MutableStateFlow(BottomNavTab.DASHBOARD)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    fun selectTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }
}


