package com.el.planora.data.remote.model

import com.google.gson.annotations.SerializedName

// ── Study Q&A ─────────────────────────────────────────────────────────────────
// POST /qa uses query parameters, not a body — handled in the service interface

data class QaResponse(
    val question: String,
    val answer: String,
    val subject: String
)

data class QaRequest(
    val user_id: String,
    val question: String,
    val subject_name: String,
    val user_category: String,
    val context: String? = null
)