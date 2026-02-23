package com.el.studyflow.domain

import com.el.studyflow.domain.model.AuthState
import com.el.studyflow.domain.model.User

interface AuthRepository {
    suspend fun signUp(username: String, email: String, password: String): AuthState<User>
    suspend fun login(email: String, password: String): AuthState<User>
    suspend fun logout()
    fun getCurrentUser(): User?
}
