package com.el.planora.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashDestination { LOADING, ONBOARDING, HOME }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _destination = MutableStateFlow(SplashDestination.LOADING)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            // Wait for animation to finish before deciding
            delay(1400)

            val currentUser = auth.currentUser
            _destination.value = if (currentUser != null) {
                SplashDestination.HOME       // already logged in → go to dashboard
            } else {
                SplashDestination.ONBOARDING // not logged in → go to onboarding
            }
        }
    }
}