package com.el.planora.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.el.planora.domain.model.AuthState
import com.el.planora.domain.model.User
import com.el.planora.domain.usecase.GetCurrentUserUseCase
import com.el.planora.domain.usecase.LoginUseCase
import com.el.planora.domain.usecase.LogoutUseCase
import com.el.planora.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpFormState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
)

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
)

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _signUpForm = MutableStateFlow(SignUpFormState())
    val signUpForm: StateFlow<SignUpFormState> = _signUpForm.asStateFlow()

    private val _loginForm = MutableStateFlow(LoginFormState())
    val loginForm: StateFlow<LoginFormState> = _loginForm.asStateFlow()

    fun onSignUpUsernameChange(value: String) =
        _signUpForm.update { it.copy(username = value, usernameError = null) }

    fun onSignUpEmailChange(value: String) =
        _signUpForm.update { it.copy(email = value, emailError = null) }

    fun onSignUpPasswordChange(value: String) =
        _signUpForm.update { it.copy(password = value, passwordError = null) }

    fun onSignUpTogglePassword() =
        _signUpForm.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun onLoginEmailChange(value: String) =
        _loginForm.update { it.copy(email = value, emailError = null) }

    fun onLoginPasswordChange(value: String) =
        _loginForm.update { it.copy(password = value, passwordError = null) }

    fun onLoginTogglePassword() =
        _loginForm.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun signUp() {
        if (!validateSignUp()) return
        val form = _signUpForm.value
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            _authState.value = when (val result = signUpUseCase(form.username, form.email, form.password)) {
                is AuthState.Success -> AuthUiState.Success(result.data)
                is AuthState.Error   -> AuthUiState.Error(result.message)
                is AuthState.Loading -> AuthUiState.Loading
            }
        }
    }

    fun login() {
        if (!validateLogin()) return
        val form = _loginForm.value
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            _authState.value = when (val result = loginUseCase(form.email, form.password)) {
                is AuthState.Success -> AuthUiState.Success(result.data)
                is AuthState.Error   -> AuthUiState.Error(result.message)
                is AuthState.Loading -> AuthUiState.Loading
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _authState.value = AuthUiState.Idle
        }
    }

    fun resetState() { _authState.value = AuthUiState.Idle }

    private fun validateSignUp(): Boolean {
        val form = _signUpForm.value
        var valid = true
        if (form.username.isBlank()) {
            _signUpForm.update { it.copy(usernameError = "Username is required") }
            valid = false
        }
        if (form.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            _signUpForm.update { it.copy(emailError = "Enter a valid email address") }
            valid = false
        }
        if (form.password.length < 6) {
            _signUpForm.update { it.copy(passwordError = "Password must be at least 6 characters") }
            valid = false
        }
        return valid
    }

    private fun validateLogin(): Boolean {
        val form = _loginForm.value
        var valid = true
        if (form.email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(form.email).matches()) {
            _loginForm.update { it.copy(emailError = "Enter a valid email address") }
            valid = false
        }
        if (form.password.isBlank()) {
            _loginForm.update { it.copy(passwordError = "Password is required") }
            valid = false
        }
        return valid
    }
}