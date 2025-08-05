package com.interviewmate.controller

import com.interviewmate.repository.UserRepository
import com.interviewmate.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subscription")
class SubscriptionController(
    private val userRepository: UserRepository
) {
    data class SubscriptionStatusResponse(val subscribed: Boolean)

    /**
     * Simulates subscribing the current user.
     * TODO Replace with real payment provider integration (e.g., Stripe webhook).
     */
    @PostMapping("/subscribe")
    fun subscribe(): ResponseEntity<SubscriptionStatusResponse> {
        val auth = SecurityContextHolder.getContext().authentication
        val claims = auth?.principal as? JwtUtil.JwtClaims
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val user = userRepository.findById(claims.userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        val updated = if (user.subscriptionStatus == "SUBSCRIBED") user else user.copy(subscriptionStatus = "SUBSCRIBED")
        userRepository.save(updated)
        return ResponseEntity.ok(SubscriptionStatusResponse(true))
    }

    /**
     * Returns subscription status for the current user.
     * TODO Replace with real payment provider integration.
     */
    @GetMapping("/status")
    fun status(): ResponseEntity<SubscriptionStatusResponse> {
        val auth = SecurityContextHolder.getContext().authentication
        val claims = auth?.principal as? JwtUtil.JwtClaims
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val user = userRepository.findById(claims.userId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        val subscribed = user.subscriptionStatus == "SUBSCRIBED"
        return ResponseEntity.ok(SubscriptionStatusResponse(subscribed))
    }
}

