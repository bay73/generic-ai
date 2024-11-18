package com.bay.aiclient.api.cerebras

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

class CerebrasClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://api.cerebras.ai/v1/models"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .andRespondOk(
                        """{
                            "object": "list",
                            "data": [
                                {
                                    "id": "llama3.1-8b",
                                    "object": "model",
                                    "created": 1721692800,
                                    "owned_by": "Meta"
                                },
                                {
                                    "id": "llama3.1-70b",
                                    "object": "model",
                                    "created": 1721692800,
                                    "owned_by": "Meta"
                                }
                            ]
                        }""",
                    )

            val client =
                CerebrasClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels =
                listOf(
                    CerebrasModel("llama3.1-8b", "llama3.1-8b", 1721692800),
                    CerebrasModel("llama3.1-70b", "llama3.1-70b", 1721692800),
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
                    .expect(postRequestTo("https://api.cerebras.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[{"role":"user", "content":"Question"}],
                                "model":"test-model"
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "id": "chatcmpl-292e278f-514e-4186-9010-91ce6a14168b",
                            "choices": [
                                {
                                    "finish_reason": "stop",
                                    "index": 0,
                                    "message": {
                                        "content": "Hello! How can I assist you today?",
                                        "role": "assistant"
                                    }
                                }
                            ],
                            "created": 1723733419,
                            "model": "llama3.1-8b",
                            "system_fingerprint": "fp_70185065a4",
                            "object": "chat.completion",
                            "usage": {
                                "prompt_tokens": 12,
                                "completion_tokens": 10,
                                "total_tokens": 22
                            },
                            "time_info": {
                                "queue_time": 0.000073161,
                                "prompt_time": 0.0010744798888888889,
                                "completion_time": 0.005658071111111111,
                                "total_time": 0.022224903106689453,
                                "created": 1723733419
                            }
                        }""",
                    )

            val client =
                CerebrasClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("Hello! How can I assist you today?", response.response)
            assertEquals(12, response.usage?.inputTokens)
            assertEquals(10, response.usage?.outputTokens)
            assertEquals(22, response.usage?.totalTokens)
            assertEquals("stop", response.choices?.first()?.finish_reason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.cerebras.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user", "content":"first question"},
                                    {"role":"assistant", "content":"first answer"},
                                    {"role":"system", "content":"Instructions"},
                                    {"role":"user", "content":"Question"}
                                ],
                                "model":"test-model",
                                "max_completion_tokens":1000,
                                "response_format":{"type":"json_object"},
                                "seed": 10,
                                "stop":["bad word","stop word"],
                                "temperature":0.33,
                                "top_p":0.55
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                CerebrasClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    CerebrasGenerateTextRequest
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
                            seed = 10
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }

    @Test
    fun genericGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.cerebras.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user", "content":"Question A"},
                                    {"role":"assistant", "content":"Answer A"},
                                    {"role":"system", "content":"System Instructions"},
                                    {"role":"user", "content":"Generic Question"}
                                ],
                                "model":"generic-model",
                                "max_completion_tokens":2000,
                                "stop":["bad","stop"],
                                "temperature":0.66,
                                "top_p":0.77
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                CerebrasClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
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
                    .expect(postRequestTo("https://api.cerebras.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[{"role":"user", "content":"Question"}],
                                "model":"default-model",
                                "temperature":0.66
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                CerebrasClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
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
