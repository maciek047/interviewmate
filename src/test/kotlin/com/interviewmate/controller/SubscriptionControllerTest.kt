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
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.Optional

@WebMvcTest(SubscriptionController::class)
@Import(SecurityConfig::class)
@org.springframework.test.context.TestPropertySource(properties = [
    "jwt.secret=0123456789ABCDEF0123456789ABCDEF",
    "jwt.expiration-ms=3600000"
])
class SubscriptionControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

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
    @AfterEach
    fun clearContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext()
    }

    @Test
    fun `subscribe unauthenticated returns 401`() {
        mockMvc.perform(post("/api/subscription/subscribe"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `subscribe authenticated updates status`() {
        val user = User(id = 1, email = "a@example.com", passwordHash = "h")
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
        whenever(userRepository.save(any())).thenAnswer { it.getArgument<User>(0) }

        val claims = JwtUtil.JwtClaims(1, "NONE")
        val auth = UsernamePasswordAuthenticationToken(claims, null, listOf())

        mockMvc.perform(post("/api/subscription/subscribe").with(authentication(auth)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subscribed").value(true))

        val captor = ArgumentCaptor.forClass(User::class.java)
        verify(userRepository).save(captor.capture())
        assertEquals("SUBSCRIBED", captor.value.subscriptionStatus)
    }

    @Test
    fun `status unauthenticated returns 401`() {
        mockMvc.perform(get("/api/subscription/status"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `status authenticated returns flag`() {
        val user = User(id = 2, email = "b@example.com", passwordHash = "h", subscriptionStatus = "SUBSCRIBED")
        whenever(userRepository.findById(2L)).thenReturn(Optional.of(user))

        val claims = JwtUtil.JwtClaims(2, "SUBSCRIBED")
        val auth = UsernamePasswordAuthenticationToken(claims, null, listOf())

        mockMvc.perform(get("/api/subscription/status").with(authentication(auth)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.subscribed").value(true))
    }
}

