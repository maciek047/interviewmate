package com.interviewmate

import com.fasterxml.jackson.databind.ObjectMapper
import com.interviewmate.service.LLMService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @MockBean
    lateinit var llmService: LLMService

    @Test
    fun `full user flow`() {
        val questions = (1..5).map { i -> "Q$i" to "A$i" }
        given(llmService.generateQuestions(anyString(), anyString(), anyInt())).willReturn(questions)

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"flow@example.com","password":"pass"}""")
        ).andExpect(status().isCreated)

        val loginRes = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"flow@example.com","password":"pass"}""")
        ).andExpect(status().isOk)
            .andReturn().response.contentAsString
        val token = mapper.readTree(loginRes).get("token").asText()

        mockMvc.perform(
            post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.questions.length()").value(3))
            .andExpect(jsonPath("$.subscribed").value(false))

        mockMvc.perform(
            post("/api/subscription/subscribe")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)

        mockMvc.perform(
            post("/api/questions")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.questions.length()").value(5))
            .andExpect(jsonPath("$.subscribed").value(true))
    }
}
