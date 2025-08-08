package com.interviewmate.util

import java.net.URI

object DatabaseUrlParser {
    fun toJdbcUrl(url: String): String {
        return if (url.startsWith("postgres://")) {
            val uri = URI(url)
            val userInfo = uri.userInfo?.split(":") ?: throw IllegalArgumentException("Invalid DATABASE_URL")
            val user = userInfo[0]
            val pass = userInfo[1]
            "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}?user=$user&password=$pass&sslmode=require"
        } else {
            url
        }
    }
}
