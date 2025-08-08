package com.interviewmate.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

/**
 * LLMService implementation using OpenAI's Chat Completion API.
 */
@Service
class OpenAiService(
    builder: WebClient.Builder,
    @Value("\${openai.api-key}") openAiApiKey: String,
    private val mapper: ObjectMapper
) : LLMService {
    private val client: WebClient = builder
        .baseUrl("https://api.openai.com")
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $openAiApiKey")
        .build()

    override suspend fun generateQuestions(
        jobName: String,
        jobDescription: String,
        numQuestions: Int
    ): List<Pair<String, String>> {
        val request = mapOf(
            "model" to "gpt-3.5-turbo",
            "temperature" to 0.7,
            "messages" to listOf(
                mapOf(
                    "role" to "system",
                    "content" to "You are an expert tech recruiter helping candidates practice for interviews. You generate interview questions and answers given a job description."
                ),
                mapOf(
                    "role" to "user",
                    "content" to "Job Title: $jobName\nJob Description: $jobDescription\n\nGenerate $numQuestions interview questions with answers in JSON format: an array of objects with 'question' and 'answer'."
                )
            )
        )

        val response = try {
            client.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .awaitBody<OpenAiResponse>()
        } catch (ex: Exception) {
            throw IllegalStateException("Failed to call OpenAI", ex)
        }

        val content = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("Missing content in OpenAI response")

        val jsonNode = try {
            mapper.readTree(content)
        } catch (ex: Exception) {
            throw IllegalStateException("Malformed OpenAI response", ex)
        }
        if (!jsonNode.isArray) {
            throw IllegalStateException("Malformed OpenAI response")
        }

        return jsonNode.map { node ->
            val question = node.get("question")?.asText()
                ?: throw IllegalStateException("Malformed OpenAI response")
            val answer = node.get("answer")?.asText() ?: ""
            question to answer
        }
    }

    data class OpenAiResponse(val choices: List<Choice>) {
        data class Choice(val message: Message)
        data class Message(val content: String)
    }
}

