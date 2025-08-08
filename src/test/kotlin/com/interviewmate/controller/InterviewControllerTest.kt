package com.interviewmate.controller

import com.interviewmate.repository.InterviewSessionRepository
import com.interviewmate.repository.UserRepository
import com.interviewmate.security.JwtAuthenticationFilter
import com.interviewmate.security.JwtUtil
import com.interviewmate.security.SecurityConfig
import com.interviewmate.service.LLMService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@WebMvcTest(InterviewController::class)
@Import(SecurityConfig::class)
@org.springframework.test.context.TestPropertySource(properties = [
    "jwt.secret=0123456789ABCDEF0123456789ABCDEF",
    "jwt.expiration-ms=3600000"
])
class InterviewControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @MockBean
    lateinit var sessionRepository: InterviewSessionRepository

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var jwtFilter: JwtAuthenticationFilter

    @org.junit.jupiter.api.BeforeEach
    fun setupFilter() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext()
        org.mockito.kotlin.doAnswer {
            val req = it.getArgument<jakarta.servlet.ServletRequest>(0)
            val res = it.getArgument<jakarta.servlet.ServletResponse>(1)
            val chain = it.getArgument<jakarta.servlet.FilterChain>(2)
            chain.doFilter(req, res)
            null
        }.whenever(jwtFilter).doFilter(any(), any(), any())
    }

    companion object {
        val questions = listOf(
            "Q1" to "A1",
            "Q2" to "A2",
            "Q3" to "A3",
            "Q4" to "A4",
            "Q5" to "A5"
        )
    }

    @org.springframework.boot.test.context.TestConfiguration
    class LlmConfig {
        @Bean
        fun llmService(): LLMService = object : LLMService {
            override fun generateQuestions(jobName: String, jobDescription: String, numQuestions: Int): List<Pair<String, String>> = questions
        }
    }

    @AfterEach
    fun clearContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext()
    }

    @Test
    fun `unauthenticated limited to three`() {
        val res = mockMvc.perform(
            post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        )
            .andExpect(status().isOk)
            .andReturn()
        val node = mapper.readTree(res.response.contentAsString)
        assertEquals(3, node.get("questions").size())
        assertEquals(false, node.get("subscribed").asBoolean())
        assertEquals("Q1", node.get("questions")[0].get("question").asText())
        assertEquals("Q3", node.get("questions")[2].get("question").asText())
    }

    @Test
    fun `authenticated unsubscribed limited to three`() {
        val user = com.interviewmate.model.User(id = 1, email = "a@b.com", passwordHash = "h")
        whenever(userRepository.findById(any())).thenReturn(Optional.of(user))

        val claims = JwtUtil.JwtClaims(1, "NONE")
        val auth = UsernamePasswordAuthenticationToken(claims, null, listOf())

        val res = mockMvc.perform(
            post("/api/questions")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        )
            .andExpect(status().isOk)
            .andReturn()
        val node = mapper.readTree(res.response.contentAsString)
        assertEquals(3, node.get("questions").size())
        assertEquals(false, node.get("subscribed").asBoolean())
    }

    @Test
    fun `subscribed user gets full list`() {
        val user = com.interviewmate.model.User(id = 2, email = "b@c.com", passwordHash = "h", subscriptionStatus = "SUBSCRIBED")
        whenever(userRepository.findById(any())).thenReturn(Optional.of(user))

        val claims = JwtUtil.JwtClaims(2, "SUBSCRIBED")
        val auth = UsernamePasswordAuthenticationToken(claims, null, listOf())

        val res = mockMvc.perform(
            post("/api/questions")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        )
            .andExpect(status().isOk)
            .andReturn()
        val node = mapper.readTree(res.response.contentAsString)
        assertEquals(5, node.get("questions").size())
        assertEquals(true, node.get("subscribed").asBoolean())
    }

    @Test
    fun `invalid input returns bad request`() {
        mockMvc.perform(
            post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"","jobDescription":"Desc"}""")
        )
            .andExpect(status().isBadRequest)
    }
}
