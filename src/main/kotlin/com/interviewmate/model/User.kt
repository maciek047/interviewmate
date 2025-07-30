package com.interviewmate.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:Email
    @Column(unique = true)
    val email: String = "",

    val passwordHash: String = "",

    val role: String = "USER",

    val subscriptionStatus: String = "NONE",

    val createdAt: Instant = Instant.now()
)
