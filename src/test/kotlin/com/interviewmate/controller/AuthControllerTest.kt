package com.interviewmate.controller

import com.interviewmate.model.User
import com.interviewmate.repository.UserRepository
import com.interviewmate.security.JwtAuthenticationFilter
import com.interviewmate.security.JwtUtil
import com.interviewmate.security.SecurityConfig
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class)
class AuthControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var userRepository: UserRepository

    @MockBean
    lateinit var passwordEncoder: PasswordEncoder

    @MockBean
    lateinit var jwtUtil: JwtUtil

    @MockBean
    lateinit var jwtFilter: JwtAuthenticationFilter

    @Test
    fun `successful registration`() {
        given(userRepository.findByEmail("new@example.com")).willReturn(null)
        given(passwordEncoder.encode("pass")).willReturn("hash")
        given(userRepository.save(any())).willReturn(User(id = 1, email = "new@example.com", passwordHash = "hash"))

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"new@example.com","password":"pass"}""")
        )
            .andExpect(status().isCreated)
    }

    @Test
    fun `registration conflict`() {
        given(userRepository.findByEmail("exists@example.com")).willReturn(User(id = 2, email = "exists@example.com", passwordHash = "h"))

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"exists@example.com","password":"pass"}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `successful login`() {
        val user = User(id = 3, email = "u@example.com", passwordHash = "hash")
        given(userRepository.findByEmail("u@example.com")).willReturn(user)
        given(passwordEncoder.matches("pass", "hash")).willReturn(true)
        given(jwtUtil.createToken(3, user.subscriptionStatus)).willReturn("tok")

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"u@example.com","password":"pass"}""")
        )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("tok")))
    }

    @Test
    fun `login failure`() {
        val user = User(id = 4, email = "u@example.com", passwordHash = "hash")
        given(userRepository.findByEmail("u@example.com")).willReturn(user)
        given(passwordEncoder.matches("bad", "hash")).willReturn(false)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"email":"u@example.com","password":"bad"}""")
        )
            .andExpect(status().isUnauthorized)
    }
}
