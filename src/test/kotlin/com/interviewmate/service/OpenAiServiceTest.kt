package com.interviewmate.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class OpenAiServiceTest {
    private fun buildService(body: String): OpenAiService {
        val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val exchange = ExchangeFunction {
            Mono.just(
                ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .body(body)
                    .build()
            )
        }
        val builder = WebClient.builder()
            .exchangeFunction(exchange)
            .codecs { it.defaultCodecs().jackson2JsonDecoder(org.springframework.http.codec.json.Jackson2JsonDecoder(mapper)) }
        return OpenAiService(builder, "key", mapper)
    }

    @Test
    fun `parse valid response`() {
        val content = "[{\"question\":\"Q1\",\"answer\":\"A1\"}]"
        val body = ObjectMapper().writeValueAsString(
            mapOf("choices" to listOf(mapOf("message" to mapOf("content" to content))))
        )
        val service = buildService(body)

        val result = service.generateQuestions("dev", "desc", 1)
        assertEquals(listOf("Q1" to "A1"), result)
    }

    @Test
    fun `throws on malformed json`() {
        val body = "{\"choices\":[{\"message\":{\"content\":\"not json\"}}]}"
        val service = buildService(body)

        assertThrows(IllegalStateException::class.java) {
            service.generateQuestions("dev", "desc", 1)
        }
    }
}

