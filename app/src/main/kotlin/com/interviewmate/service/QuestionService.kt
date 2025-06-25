package com.interviewmate.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Service

@Service
class QuestionService {
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val apiKey: String = System.getenv("OPENAI_API_KEY") ?: ""

    fun generateQuestions(jobName: String, jobDetails: String): List<String> {
        require(apiKey.isNotBlank()) { "OPENAI_API_KEY not configured" }
        val prompt = "Generate interview questions for the following job. Job name: $jobName. Job details: $jobDetails"
        val payload = mapper.createObjectNode().apply {
            put("model", "gpt-3.5-turbo")
            putArray("messages").addObject().put("role", "user").put("content", prompt)
        }
        val body = payload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("OpenAI request failed: ${'$'}{response.code}")
            }
            val json: JsonNode = mapper.readTree(response.body!!.string())
            val content = json["choices"]?.get(0)?.get("message")?.get("content")?.asText() ?: ""
            return content.lines().map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}
