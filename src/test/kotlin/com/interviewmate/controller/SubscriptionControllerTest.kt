package com.interviewmate.controller

import com.interviewmate.model.User
import com.interviewmate.repository.UserRepository
import com.interviewmate.security.JwtAuthenticationFilter
import com.interviewmate.security.JwtUtil
import com.interviewmate.security.SecurityConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@WebMvcTest(SubscriptionController::class)
@Import(SecurityConfig::class)
class SubscriptionControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var jwtFilter: JwtAuthenticationFilter

    @AfterEach
    fun clearContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `subscribe unauthenticated returns 401`() {
        mockMvc.perform(post("/api/subscription/subscribe"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `subscribe authenticated updates status`() {
        val user = User(id = 1, email = "a@example.com", passwordHash = "h")
        given(userRepository.findById(1)).willReturn(Optional.of(user))
        given(userRepository.save(any())).willAnswer { it.getArgument<User>(0) }

        val claims = JwtUtil.JwtClaims(1, "NONE")
        val auth = UsernamePasswordAuthenticationToken(claims, null, listOf())
        SecurityContextHolder.getContext().authentication = auth

        mockMvc.perform(post("/api/subscription/subscribe"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subscribed").value(true))

        val captor = ArgumentCaptor.forClass(User::class.java)
        verify(userRepository).save(captor.capture())
        assertEquals("SUBSCRIBED", captor.value.subscriptionStatus)
    }

    @Test
    fun `status unauthenticated returns 401`() {
        mockMvc.perform(get("/api/subscription/status"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `status authenticated returns flag`() {
        val user = User(id = 2, email = "b@example.com", passwordHash = "h", subscriptionStatus = "SUBSCRIBED")
        given(userRepository.findById(2)).willReturn(Optional.of(user))

        val claims = JwtUtil.JwtClaims(2, "SUBSCRIBED")
        val auth = UsernamePasswordAuthenticationToken(claims, null, listOf())
        SecurityContextHolder.getContext().authentication = auth

        mockMvc.perform(get("/api/subscription/status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subscribed").value(true))
    }
}

