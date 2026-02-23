package com.el.studyflow.domain.usecase

import com.el.studyflow.domain.model.User
import com.el.studyflow.domain.AuthRepository
import com.el.studyflow.domain.model.AuthState
import com.google.firebase.auth.AuthResult
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, email: String, password: String): AuthState<User> =
        repository.signUp(username, email, password)
}

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthState<User> =
        repository.login(email, password)
}

class LogoutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}

class GetCurrentUserUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(): User? = repository.getCurrentUser()
}