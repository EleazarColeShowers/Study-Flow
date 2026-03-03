package com.el.planora.domain.model

// Stores the answers from the setup questionnaire
data class UserProfile(
    val learningDifferences: List<String> = emptyList(), // Q1 is multi-select
    val attentionSpan: String = "",
    val studyTime: String = "",
    val dailyDedication: String = "",
    val studyGoal: String = ""
)