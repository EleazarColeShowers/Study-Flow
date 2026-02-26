package com.el.studyflow.domain.model

data class StudyTrack(
    val id: String,
    val title: String,
    val exam: String, // "JAMB Preparation", etc.
    val subject: String,
    val coverImageUrl: String, // placeholder URL for now
    val estimatedHours: Int,
    val completionPercentage: Int, // 0-100
    val lastActiveDate: Long,
    val topics: List<Topic>
)

data class Topic(
    val id: String,
    val name: String,
    val estimatedHours: Int,
    val isCompleted: Boolean,
    val resources: List<String>
)

data class DashboardStats(
    val dayStreak: Int,
    val activePlans: Int,
    val averageProgress: Int // 0-100
)