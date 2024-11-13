package com.bay.aiclient.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class GenericGenerateTextRequestTest {
    @Test
    fun commonBuilderWithoutModel_throws() {
        assertFailsWith(IllegalArgumentException::class, "Model should be set!") {
            GenerateTextRequest
                .Builder()
                .also {
                    it.prompt = "Some prompt"
                }.build()
        }
    }

    @Test
    fun commonBuilder_createsGenericRequest() {
        val builder =
            GenerateTextRequest.Builder().also {
                it.model = "test_model"
                it.prompt = "Some prompt"
                it.systemInstructions = "Instructions"
                it.chatHistory = listOf(TextMessage("user", "first question"), TextMessage("assistant", "first answer"))
                it.stopSequences = listOf("bad word", "stop word")
                it.temperature = 0.1
                it.maxOutputTokens = 1000
                it.topP = 0.5
            }

        val request = builder.build()

        assertIs<GenericGenerateTextRequest>(request)
        assertEquals("test_model", request.model)
        assertEquals("Some prompt", request.prompt)
        assertEquals("Instructions", request.systemInstructions)
        assertEquals(listOf(TextMessage("user", "first question"), TextMessage("assistant", "first answer")), request.chatHistory)
        assertEquals(listOf("bad word", "stop word"), request.stopSequences)
        assertEquals(0.1, request.temperature)
        assertEquals(1000, request.maxOutputTokens)
        assertEquals(0.5, request.topP)
    }
}
