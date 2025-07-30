package com.interviewmate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InterviewMateApplication

// Entry point for the Spring Boot application
fun main(args: Array<String>) {
    runApplication<InterviewMateApplication>(*args)
}

// TODO Add controllers and services in future tasks
