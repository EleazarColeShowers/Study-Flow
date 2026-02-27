//package com.el.studyflow.data.repository
//
//import com.el.studyflow.domain.model.AuthState
//import com.el.studyflow.domain.model.UserProfile
//import com.el.studyflow.domain.repository.ProfileRepository
//import com.google.firebase.Timestamp
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.SetOptions
//import kotlinx.coroutines.tasks.await
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class ProfileRepositoryImpl @Inject constructor(
//    private val firestore: FirebaseFirestore
//) : ProfileRepository {
//
//    override suspend fun saveUserProfile(profile: UserProfile): AuthState<Unit> {
//        return try {
//            val data = mapOf(
//                "uid"                 to profile.uid,
//                "learningDifferences" to profile.learningDifferences,
//                "attentionSpan"       to profile.attentionSpan,
//                "studyTime"           to profile.studyTime,
//                "dailyDedication"     to profile.dailyDedication,
//                "studyGoal"           to profile.studyGoal,
//                "studySubject"        to profile.studySubject,
//                "updatedAt"           to Timestamp.now()
//            )
//            // merge = don't overwrite username/email set during sign up
//            firestore.collection("users")
//                .document(profile.uid)
//                .set(data, SetOptions.merge())
//                .await()
//
//            AuthState.Success(Unit)
//        } catch (e: Exception) {
//            AuthState.Error(e.message ?: "Failed to save profile.")
//        }
//    }
//
//    override suspend fun getUserProfile(uid: String): AuthState<UserProfile> {
//        return try {
//            val doc = firestore.collection("users")
//                .document(uid)
//                .get()
//                .await()
//
//            @Suppress("UNCHECKED_CAST")
//            val profile = UserProfile(
//                uid           = uid,
//                learningDifferences = (doc.get("learningDifferences") as? List<String>) ?: emptyList(),
//                attentionSpan = doc.getString("attentionSpan") ?: "",
//                studyTime     = doc.getString("studyTime") ?: "",
//                dailyDedication = doc.getString("dailyDedication") ?: "",
//                studyGoal     = doc.getString("studyGoal") ?: "",
//                studySubject  = doc.getString("studySubject") ?: ""
//            )
//
//            AuthState.Success(profile)
//        } catch (e: Exception) {
//            AuthState.Error(e.message ?: "Failed to load profile.")
//        }
//    }
//}