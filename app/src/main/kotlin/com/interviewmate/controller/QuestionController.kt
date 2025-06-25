package com.interviewmate.controller

import com.interviewmate.service.QuestionService
import org.springframework.web.bind.annotation.*

data class QuestionRequest(val jobName: String, val jobDetails: String)
data class QuestionResponse(val questions: List<String>)

@RestController
@RequestMapping("/api/questions")
class QuestionController(private val questionService: QuestionService) {
    @PostMapping
    fun generateQuestions(@RequestBody request: QuestionRequest): QuestionResponse {
        val qs = questionService.generateQuestions(request.jobName, request.jobDetails)
        return QuestionResponse(qs)
    }
}
