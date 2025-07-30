package com.interviewmate.controller

import com.interviewmate.model.User
import com.interviewmate.repository.UserRepository
import com.interviewmate.security.JwtUtil
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    data class RegisterRequest(
        @field:Email val email: String = "",
        @field:NotBlank val password: String = ""
    )

    data class LoginRequest(
        @field:Email val email: String = "",
        @field:NotBlank val password: String = ""
    )

    data class AuthResponse(val token: String, val subscriptionStatus: String)

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<Any> {
        if (userRepository.findByEmail(request.email) != null) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Email already in use"))
        }
        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)
        )
        val saved = userRepository.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("id" to saved.id))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<Any> {
        val user = userRepository.findByEmail(request.email)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val token = jwtUtil.createToken(user.id, user.subscriptionStatus)
        return ResponseEntity.ok(AuthResponse(token, user.subscriptionStatus))
    }
}
