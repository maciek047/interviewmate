package com.interviewmate.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtUtilTest {
    // At least 32 bytes required for HS256 signing key
    private val util = JwtUtil("0123456789ABCDEF0123456789ABCDEF", 3600000)

    @Test
    fun `create and parse token`() {
        val token = util.createToken(1, "SUBSCRIBED")
        val claims = util.parseToken(token)
        assertNotNull(claims)
        assertEquals(1, claims!!.userId)
        assertEquals("SUBSCRIBED", claims.subscriptionStatus)
    }

    @Test
    fun `invalid token returns null`() {
        val claims = util.parseToken("bad.token.here")
        assertNull(claims)
    }
}
