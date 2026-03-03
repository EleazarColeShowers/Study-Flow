package com.el.planora.domain.model


data class User(
    val uid: String,
    val username: String,
    val email: String
)

sealed class AuthState<out T> {
    data class Success<T>(val data: T) : AuthState<T>()
    data class Error(val message: String) : AuthState<Nothing>()
    data object Loading : AuthState<Nothing>()
}