package com.interviewmate.service

import com.theokanning.openai.service.OpenAiService
import com.theokanning.openai.completion.chat.ChatCompletionRequest
import com.theokanning.openai.completion.chat.ChatCompletionResult
import com.theokanning.openai.completion.chat.ChatMessage
import org.springframework.stereotype.Service

@Service
class AIService {
    private val client: OpenAiService

    init {
        val token = System.getenv("OPENAI_API_KEY")
            ?: throw IllegalStateException("OPENAI_API_KEY environment variable not set")
        client = OpenAiService(token)
    }

    fun chat(messages: List<ChatMessage>): ChatCompletionResult {
        val request = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            .build()
        return client.createChatCompletion(request)
    }
}
