package com.interviewmate.service

/**
 * Abstraction over Large Language Models capable of generating
 * interview questions and answers from a job title and description.
 *
 * @param jobName name of the job role
 * @param jobDescription details of the job listing
 * @param numQuestions number of Q&A pairs to generate, default is 5
 *
 * @return list of question and answer pairs
 *
 * TODO: Support other providers like Claude, Gemini or Bedrock via
 * dedicated implementations selectable by Spring profiles or
 * qualifiers.
 */
interface LLMService {
    fun generateQuestions(
        jobName: String,
        jobDescription: String,
        numQuestions: Int = 5
    ): List<Pair<String, String>>
}

