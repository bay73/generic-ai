package com.bay.aiclient.api.anthropic

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.ResponseFormat
import com.bay.aiclient.domain.TextMessage
import com.bay.aiclient.utils.MockHttpEngine
import com.bay.aiclient.utils.RequestMatcher.Companion.header
import com.bay.aiclient.utils.RequestMatcher.Companion.jsonBody
import com.bay.aiclient.utils.RequestMatcher.Companion.postRequestTo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnthropicClientTest {
    @Test
    fun listModels_returnsStaticList() =
        runTest {
            val expectedModels =
                listOf(
                    AnthropicModel("claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet"),
                    AnthropicModel("claude-3-5-haiku-20241022", "Claude 3.5 Haiku"),
                    AnthropicModel("claude-3-opus-20240229", "Claude 3 Opus"),
                    AnthropicModel("claude-3-sonnet-20240229", "Claude 3 Sonnet"),
                    AnthropicModel("claude-3-haiku-20240307", "Claude 3 Haiku"),
                )

            val client = AnthropicClient.Builder().build()

            val result = client.models()

            assertTrue(result.isSuccess)
            val models = result.getOrThrow().models
            assertEquals(expectedModels, models)
        }

    @Test
    fun minimalGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.anthropic.com/v1/messages"))
                    .expect(header("x-api-key", "fake_key"))
                    .expect(header("anthropic-version", "2023-06-01"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[{"role":"user", "content":"Question"}],
                                "model":"test-model",
                                "max_tokens":4096
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "content": [
                                {
                                    "text": "Hi! My name is Claude.",
                                    "type": "text"
                                }
                            ],
                            "id": "msg_013Zva2CMHLNnXjNJJKqJ2EF",
                            "model": "claude-3-5-sonnet-20241022",
                            "role": "assistant",
                            "stop_reason": "end_turn",
                            "stop_sequence": null,
                            "type": "message",
                            "usage": {
                                "input_tokens": 2095,
                                "output_tokens": 503
                            }
                        }""",
                    )

            val client =
                AnthropicClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("Hi! My name is Claude.", response.response)
            assertEquals(2095, response.usage?.inputTokens)
            assertEquals(503, response.usage?.outputTokens)
            assertEquals(2598, response.usage?.totalTokens)
            assertEquals("end_turn", response.finishReason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.anthropic.com/v1/messages"))
                    .expect(header("x-api-key", "fake_key"))
                    .expect(header("anthropic-version", "2023-06-01"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user", "content":"first question"},
                                    {"role":"assistant", "content":"first answer"},
                                    {"role":"user", "content":"Question"}
                                ],
                                "model":"test-model",
                                "max_tokens":1000,
                                "stop_sequences":["bad word","stop word"],
                                "system": "Instructions",
                                "temperature":0.33,
                                "top_p":0.55
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                AnthropicClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText {
                    model = "test-model"
                    prompt = "Question"
                    systemInstructions = "Instructions"
                    responseFormat = ResponseFormat.JSON_OBJECT // This going to be ignored as only text is supported
                    chatHistory = listOf(TextMessage("user", "first question"), TextMessage("assistant", "first answer"))
                    maxOutputTokens = 1000
                    stopSequences = listOf("bad word", "stop word")
                    temperature = 0.33
                    topP = 0.55
                }

            assertTrue(result.isSuccess)
        }

    @Test
    fun genericGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.anthropic.com/v1/messages"))
                    .expect(header("x-api-key", "fake_key"))
                    .expect(header("anthropic-version", "2023-06-01"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user", "content":"Question A"},
                                    {"role":"assistant", "content":"Answer A"},
                                    {"role":"user", "content":"Generic Question"}
                                ],
                                "model":"generic-model",
                                "max_tokens":2000,
                                "stop_sequences":["bad","stop"],
                                "system": "System Instructions",
                                "temperature":0.66,
                                "top_p":0.77
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                AnthropicClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "default_model"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    GenerateTextRequest
                        .Builder()
                        .apply {
                            model = "generic-model"
                            prompt = "Generic Question"
                            systemInstructions = "System Instructions"
                            responseFormat = ResponseFormat.TEXT
                            chatHistory = listOf(TextMessage("user", "Question A"), TextMessage("assistant", "Answer A"))
                            maxOutputTokens = 2000
                            stopSequences = listOf("bad", "stop")
                            temperature = 0.66
                            topP = 0.77
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }

    @Test
    fun defaultClientValues_usedInTheRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.anthropic.com/v1/messages"))
                    .expect(header("x-api-key", "fake_key"))
                    .expect(header("anthropic-version", "2023-06-01"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[{"role":"user", "content":"Question"}],
                                "model":"default-model",
                                "max_tokens":4096,
                                "temperature":0.66
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                AnthropicClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "default-model"
                        defaultTemperature = 0.66
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText {
                    prompt = "Question"
                }

            assertTrue(result.isSuccess)
        }
}
