package com.interviewmate

import com.fasterxml.jackson.databind.ObjectMapper
import com.interviewmate.service.LLMService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlinx.coroutines.runBlocking
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.test.context.TestPropertySource(properties = [
    "jwt.secret=0123456789ABCDEF0123456789ABCDEF",
    "jwt.expiration-ms=3600000"
])
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
        runBlocking {
            whenever(llmService.generateQuestions("Dev", "Desc", 5)).thenReturn(questions)
        }

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

        var mvcResult = mockMvc.perform(
            post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        ).andReturn()
        var res = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andReturn()
        var node = mapper.readTree(res.response.contentAsString)
        assertEquals(3, node.get("questions").size())
        assertEquals(false, node.get("subscribed").asBoolean())

        mockMvc.perform(
            post("/api/subscription/subscribe")
                .header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)

        mvcResult = mockMvc.perform(
            post("/api/questions")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"jobName":"Dev","jobDescription":"Desc","numQuestions":5}""")
        ).andReturn()
        res = mockMvc.perform(asyncDispatch(mvcResult))
            .andExpect(status().isOk)
            .andReturn()
        node = mapper.readTree(res.response.contentAsString)
        assertEquals(5, node.get("questions").size())
        assertEquals(true, node.get("subscribed").asBoolean())
    }
}
