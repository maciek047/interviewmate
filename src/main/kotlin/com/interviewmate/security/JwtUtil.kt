package com.interviewmate.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.Date

@Component
class JwtUtil(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {
    private val key: Key = Keys.hmacShaKeyFor(secret.toByteArray())

    data class JwtClaims(val userId: Long, val subscriptionStatus: String)

    fun createToken(userId: Long, subscriptionStatus: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("subStatus", subscriptionStatus)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun parseToken(token: String): JwtClaims? = try {
        val body = Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
        val id = body.subject.toLong()
        val status = body["subStatus"] as String
        JwtClaims(id, status)
    } catch (ex: Exception) {
        null
    }
}
