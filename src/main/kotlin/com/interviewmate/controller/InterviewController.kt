package com.interviewmate.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.interviewmate.dto.GenerateRequest
import com.interviewmate.dto.GenerateResponse
import com.interviewmate.dto.QA
import com.interviewmate.model.InterviewSession
import com.interviewmate.repository.InterviewSessionRepository
import com.interviewmate.repository.UserRepository
import com.interviewmate.security.JwtUtil
import com.interviewmate.service.LLMService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class InterviewController(
    private val llmService: LLMService,
    private val sessionRepository: InterviewSessionRepository,
    private val userRepository: UserRepository,
    private val mapper: ObjectMapper
) {
    @PostMapping("/api/questions")
    fun generateQuestions(@Valid @RequestBody req: GenerateRequest): ResponseEntity<GenerateResponse> {
        val auth = SecurityContextHolder.getContext().authentication
        val claims = auth?.principal as? JwtUtil.JwtClaims

        val results = try {
            llmService.generateQuestions(req.jobName, req.jobDescription, req.numQuestions)
        } catch (ex: Exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build()
        }

        var subscribed = false
        if (claims != null) {
            val user = userRepository.findById(claims.userId).orElse(null)
            if (user != null) {
                subscribed = user.subscriptionStatus == "SUBSCRIBED"
                val session = InterviewSession(
                    user = user,
                    jobName = req.jobName,
                    jobDescription = req.jobDescription,
                    questionsJson = mapper.writeValueAsString(results)
                )
                sessionRepository.save(session)
            }
        }

        val limited = if (!subscribed && results.size > 3) results.subList(0, 3) else results
        val qas = limited.map { (q, a) -> QA(q, a) }
        val response = GenerateResponse(qas, subscribed)
        return ResponseEntity.ok(response)
    }
}
