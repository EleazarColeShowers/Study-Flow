package com.el.planora.data.repository

import android.util.Log
import com.el.planora.domain.model.AuthState
import com.el.planora.domain.model.User
import com.el.planora.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): AuthState<User> {
        return try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = result.user
                ?: return AuthState.Error("Sign up failed. Please try again.")

            AuthState.Success(
                User(
                    uid = firebaseUser.uid,
                    username = username,
                    email = email
                )
            )
        } catch (e: Exception) {
            Log.e("AUTH", "SignUp failed: ${e.javaClass.simpleName} — ${e.message}")
            AuthState.Error(e.toReadableMessage())
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): AuthState<User> {
        return try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = result.user
                ?: return AuthState.Error("Login failed. Please try again.")

            AuthState.Success(
                User(
                    uid = firebaseUser.uid,
                    username = "",
                    email = firebaseUser.email ?: ""
                )
            )
        } catch (e: Exception) {
            Log.e("AUTH", "Login failed: ${e.javaClass.simpleName} — ${e.message}")
            AuthState.Error(e.toReadableMessage())
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            username = "",
            email = firebaseUser.email ?: ""
        )
    }

    private fun Exception.toReadableMessage(): String = when {
        message?.contains("email address is already in use") == true ->
            "An account with this email already exists."
        message?.contains("badly formatted") == true ->
            "Please enter a valid email address."
        message?.contains("password is invalid") == true ||
                message?.contains("no user record") == true ||
                message?.contains("INVALID_LOGIN_CREDENTIALS") == true ->
            "Incorrect email or password."
        message?.contains("network") == true ->
            "No internet connection. Please check your network."
        else -> "Something went wrong. Please try again."
    }
}