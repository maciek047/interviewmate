package com.interviewmate.dto

/**
 * Response payload containing generated interview questions.
 */
data class QA(
    val question: String,
    val answer: String
)

data class GenerateResponse(
    val questions: List<QA>,
    val subscribed: Boolean
)
