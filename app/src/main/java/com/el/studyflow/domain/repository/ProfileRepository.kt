package com.el.studyflow.domain.repository

import com.el.studyflow.domain.model.AuthState
import com.el.studyflow.domain.model.UserProfile

interface ProfileRepository {
    suspend fun saveUserProfile(profile: UserProfile): AuthState<Unit>
    suspend fun getUserProfile(uid: String): AuthState<UserProfile>
}