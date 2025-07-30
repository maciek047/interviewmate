package com.interviewmate.repository

import com.interviewmate.model.InterviewSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InterviewSessionRepository : JpaRepository<InterviewSession, Long>
