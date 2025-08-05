package com.interviewmate.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Request payload for generating interview questions.
 */
data class GenerateRequest(
    @field:NotBlank
    val jobName: String = "",

    @field:NotBlank
    @field:Size(max = 1000)
    val jobDescription: String = "",

    @field:Min(1)
    @field:Max(10)
    val numQuestions: Int = 5
)
