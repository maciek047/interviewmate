package com.interviewmate.controller

import com.interviewmate.service.AIService
import com.theokanning.openai.completion.chat.ChatMessage
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

data class InterviewRequest(val jobName: String, val jobDetails: String)
data class InterviewResponse(val questions: List<String>)

data class CvRequest(val jobName: String)
data class CvResponse(val cv: String)

@RestController
class InterviewController(private val aiService: AIService) {

    @PostMapping("/generate-questions")
    fun generateQuestions(@RequestBody request: InterviewRequest): ResponseEntity<InterviewResponse> {
        val prompt = "Generate 10 interview questions for a position titled '${request.jobName}'. Job details: ${request.jobDetails}"
        val messages = listOf(ChatMessage("user", prompt))
        val result = aiService.chat(messages)
        val text = result.choices.first().message.content
        val questions = text.split("\n").filter { it.isNotBlank() }
        return ResponseEntity.ok(InterviewResponse(questions))
    }

    @PostMapping("/generate-cv")
    fun generateCv(@RequestBody request: CvRequest): ResponseEntity<CvResponse> {
        val prompt = "Generate a professional CV for the following role: ${request.jobName}"
        val messages = listOf(ChatMessage("user", prompt))
        val result = aiService.chat(messages)
        val text = result.choices.first().message.content
        return ResponseEntity.ok(CvResponse(text))
    }
}
