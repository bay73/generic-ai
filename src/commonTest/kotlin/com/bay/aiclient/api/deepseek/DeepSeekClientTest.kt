package com.bay.aiclient.api.deepseek

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.ResponseFormat
import com.bay.aiclient.domain.TextMessage
import com.bay.aiclient.utils.MockHttpEngine
import com.bay.aiclient.utils.RequestMatcher.Companion.getRequestTo
import com.bay.aiclient.utils.RequestMatcher.Companion.header
import com.bay.aiclient.utils.RequestMatcher.Companion.jsonBody
import com.bay.aiclient.utils.RequestMatcher.Companion.postRequestTo
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeepSeekClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://api.deepseek.com/models"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .andRespondOk(
                        """{
                            "object": "list",
                            "data": [
                                {
                                    "id": "deepseek-chat",
                                    "object": "model",
                                    "owned_by": "system"
                                },
                                {
                                    "id": "deepseek-reasoner",
                                    "object": "model",
                                    "owned_by": "deepseek"
                                }
                            ]
                        }""",
                    )

            val client =
                DeepSeekClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels =
                listOf(
                    DeepSeekModel("deepseek-chat", "deepseek-chat"),
                    DeepSeekModel("deepseek-reasoner", "deepseek-reasoner"),
                )

            val result = client.models()

            assertTrue(result.isSuccess)
            val models = result.getOrThrow().models
            assertEquals(expectedModels, models)
        }

    @Test
    fun minimalGenerateTextRequest_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.deepseek.com/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user","content":"Question"}
                                ],
                                "model":"test-model"
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "id": "chatcmpl-ATBu0ntPVD82UilqNZlgZTTfhJ0eu",
                            "object": "chat.completion",
                            "created": 1731520628,
                            "model": "gpt-4o-2024-08-06",
                            "choices": [
                                {
                                    "index": 0,
                                    "message": {
                                    "role": "assistant",
                                        "content": "The answer to the Ultimate Question of Life is famously known to be the number 42.",
                                        "refusal": null
                                    },
                                   "logprobs": null,
                                   "finish_reason": "stop"
                                }
                            ],
                            "usage": {
                                "prompt_tokens": 65,
                                "completion_tokens": 59,
                                "total_tokens": 124,
                                "prompt_tokens_details": {
                                    "cached_tokens": 0,
                                    "audio_tokens": 0
                                },
                                "completion_tokens_details": {
                                    "reasoning_tokens": 0,
                                    "audio_tokens": 0,
                                    "accepted_prediction_tokens": 0,
                                    "rejected_prediction_tokens": 0
                                }
                            },
                            "system_fingerprint": "fp_159d8341cc"
                        }""",
                    )

            val client =
                DeepSeekClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("The answer to the Ultimate Question of Life is famously known to be the number 42.", response.response)
            assertEquals(65, response.usage?.inputTokens)
            assertEquals(59, response.usage?.outputTokens)
            assertEquals(124, response.usage?.totalTokens)
            assertEquals("stop", response.choices?.first()?.finishReason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.deepseek.com/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"system","content":"Instructions"},
                                    {"role":"user","content":"first question"},
                                    {"role":"assistant","content":"first answer"},
                                    {"role":"user","content":"Question"}
                                ],
                                "model":"test-model",
                                "frequency_penalty":0.67,
                                "max_tokens":1000,
                                "presence_penalty":0.12,
                                "response_format":{"type":"json_object"},
                                "stop":["bad word","stop word"],
                                "temperature":0.33,
                                "top_p":0.55
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                DeepSeekClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    DeepSeekGenerateTextRequest
                        .builder()
                        .apply {
                            model = "test-model"
                            prompt = "Question"
                            systemInstructions = "Instructions"
                            responseFormat = ResponseFormat.JSON_OBJECT
                            chatHistory = listOf(TextMessage("user", "first question"), TextMessage("assistant", "first answer"))
                            maxOutputTokens = 1000
                            stopSequences = listOf("bad word", "stop word")
                            temperature = 0.33
                            topP = 0.55
                            frequencyPenalty = 0.67
                            presencePenalty = 0.12
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }

    @Test
    fun genericGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.deepseek.com/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"system","content":"System Instructions"},
                                    {"role":"user","content":"Question A"},
                                    {"role":"assistant","content":"Answer A"},
                                    {"role":"user","content":"Generic Question"}
                                ],
                                "model":"generic-model",
                                "max_tokens":2000,
                                "response_format":{"type":"text"},
                                "stop":["bad","stop"],
                                "temperature":0.66,
                                "top_p":0.77
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                DeepSeekClient
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
                    .expect(postRequestTo("https://api.deepseek.com/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user","content":"Question"}
                                ],
                                "model":"default-model",
                                "temperature":0.66
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                DeepSeekClient
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
