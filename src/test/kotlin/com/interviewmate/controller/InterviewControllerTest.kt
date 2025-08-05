package com.interviewmate.controller

import com.interviewmate.repository.InterviewSessionRepository
import com.interviewmate.repository.UserRepository
import com.interviewmate.security.JwtAuthenticationFilter
import com.interviewmate.security.JwtUtil
import com.interviewmate.security.SecurityConfig
import com.interviewmate.service.LLMService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@WebMvcTest(InterviewController::class)
@Import(SecurityConfig::class)
class InterviewControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var llmService: LLMService

    @MockBean
    lateinit var sessionRepository: InterviewSessionRepository

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var jwtFilter: JwtAuthenticationFilter

    private val questions = listOf(
        "Q1" to "A1",
        "Q2" to "A2",
        "Q3" to "A3",
        "Q4" to "A4",
        "Q5" to "A5"
    )

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `unauthenticated limited to three`() {
        given(llmService.generateQuestions(anyString(), anyString(), anyInt())).willReturn(questions)

        mockMvc.perform(
            post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.questions.length()").value(3))
            .andExpect(jsonPath("$.subscribed").value(false))
            .andExpect(jsonPath("$.questions[0].question").value("Q1"))
            .andExpect(jsonPath("$.questions[2].question").value("Q3"))
    }

    @Test
    fun `authenticated unsubscribed limited to three`() {
        given(llmService.generateQuestions(anyString(), anyString(), anyInt())).willReturn(questions)
        given(userRepository.findById(anyLong())).willReturn(Optional.empty())

        val claims = JwtUtil.JwtClaims(1, "NONE")
        val auth = UsernamePasswordAuthenticationToken(claims, null, listOf())
        SecurityContextHolder.getContext().authentication = auth

        mockMvc.perform(
            post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.questions.length()").value(3))
            .andExpect(jsonPath("$.subscribed").value(false))
    }

    @Test
    fun `subscribed user gets full list`() {
        given(llmService.generateQuestions(anyString(), anyString(), anyInt())).willReturn(questions)
        given(userRepository.findById(anyLong())).willReturn(Optional.empty())

        val claims = JwtUtil.JwtClaims(2, "SUBSCRIBED")
        val auth = UsernamePasswordAuthenticationToken(claims, null, listOf())
        SecurityContextHolder.getContext().authentication = auth

        mockMvc.perform(
            post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.questions.length()").value(5))
            .andExpect(jsonPath("$.subscribed").value(true))
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
