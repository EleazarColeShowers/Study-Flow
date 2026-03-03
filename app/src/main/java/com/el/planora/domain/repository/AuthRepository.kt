package com.el.planora.domain.repository

import com.el.planora.domain.model.AuthState
import com.el.planora.domain.model.User

interface AuthRepository {
    suspend fun signUp(username: String, email: String, password: String): AuthState<User>
    suspend fun login(email: String, password: String): AuthState<User>
    suspend fun logout()
    fun getCurrentUser(): User?
}