package com.interviewmate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner
import com.interviewmate.repository.UserRepository

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
    runApplication<InterviewMateApplication>(*args)
}

// TODO Add controllers and services in future tasks
