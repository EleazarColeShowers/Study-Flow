package com.el.planora.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.data.remote.planoraApiService
import com.el.planora.data.repository.UserRegistrationRepository
import com.el.planora.ui.component.BottomNavTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainAppViewModel @Inject constructor(
    private val userRegistrationRepository: UserRegistrationRepository,
    private val api: planoraApiService
) : ViewModel() {

    private val _currentTab = MutableStateFlow(BottomNavTab.DASHBOARD)
    val currentTab: StateFlow<BottomNavTab> = _currentTab.asStateFlow()

    init {

        viewModelScope.launch {

            try {
                api.ping() // wake server first
            } catch (_: Exception) { }


            userRegistrationRepository.registerWithPlanora()
        }
    }

    fun selectTab(tab: BottomNavTab) {
        _currentTab.value = tab
    }
}

