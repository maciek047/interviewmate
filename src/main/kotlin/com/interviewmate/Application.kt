package com.interviewmate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import com.interviewmate.repository.UserRepository
import com.interviewmate.util.DatabaseUrlParser

@SpringBootApplication
class InterviewMateApplication {

    @Bean
    fun initDatabase(userRepository: UserRepository): CommandLineRunner = CommandLineRunner {
        val userCount = userRepository.count()
        println("Database ready: user table rows = $userCount")
    }
}

// Entry point for the Spring Boot application
fun main(args: Array<String>) {
    val dbUrl = System.getenv("DATABASE_URL") ?: ""
    if (dbUrl.startsWith("postgres://")) {
        val jdbcUrl = DatabaseUrlParser.toJdbcUrl(dbUrl)
        System.setProperty("spring.datasource.url", jdbcUrl)
    }
    runApplication<InterviewMateApplication>(*args)
}
