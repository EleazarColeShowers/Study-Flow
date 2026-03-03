package com.el.planora.domain.repository

import com.el.planora.domain.model.AuthState
import com.el.planora.domain.model.UserProfile

interface ProfileRepository {
    suspend fun saveUserProfile(profile: UserProfile): AuthState<Unit>
    suspend fun getUserProfile(uid: String): AuthState<UserProfile>
}