package com.bay.aiclient.api.mistral

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

class MistralClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://api.mistral.ai/v1/models"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .andRespondOk(
                        """{
                            "object":"list",
                            "data":[
                                {
                                    "id":"ministral-3b-2410",
                                    "object":"model",
                                    "created":1731588548,
                                    "owned_by":"mistralai",
                                    "capabilities":{
                                        "completion_chat":true,
                                        "completion_fim":false,
                                        "function_calling":true,
                                        "fine_tuning":false,
                                        "vision":false
                                    },
                                    "name":"ministral-3b-2410",
                                    "description":"Official model",
                                    "max_context_length":131072,
                                    "default_model_temperature":0.7,
                                    "type":"base"
                                },
                                {
                                    "id":"ministral-3b-latest",
                                    "object":"model",
                                    "created":1731588548,
                                    "owned_by":"mistralai",
                                    "capabilities":{
                                        "completion_chat":true,
                                        "completion_fim":false,
                                        "function_calling":true,
                                        "fine_tuning":false,
                                        "vision":false
                                    },
                                    "name":"ministral-3b-2410",
                                    "description":"Official model",
                                    "max_context_length":131072,
                                    "aliases":["ministral-3b-2410"],
                                    "deprecation":null,
                                    "default_model_temperature":0.7,
                                    "type":"base"
                                }
                            ]
                        }
                        """.trimMargin(),
                    )

            val client =
                MistralClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels =
                listOf(
                    MistralModel("ministral-3b-2410", "ministral-3b-2410", "Official model", null, 1731588548),
                    MistralModel("ministral-3b-latest", "ministral-3b-2410", "Official model", null, 1731588548),
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
                    .expect(postRequestTo("https://api.mistral.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"test-model",
                                "messages":[{"role":"user","content":"Question"}]
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "id":"f1cba1b831014a0a943b802491674e15",
                            "object":"chat.completion",
                            "created":1731520627,
                            "model":"mistral-large-2407",
                            "choices":[{
                                "index":0,
                                "message":{
                                    "role":"assistant",
                                    "content":"The answer to the Ultimate Question of Life, the Universe, and Everything is... 42",
                                    "tool_calls":null
                                },
                                "finish_reason":"stop"
                            }],
                            "usage":{
                                "prompt_tokens":71,
                                "total_tokens":117,
                                "completion_tokens":46
                            }
                        }""",
                    )

            val client =
                MistralClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("The answer to the Ultimate Question of Life, the Universe, and Everything is... 42", response.response)
            assertEquals(71, response.usage?.inputTokens)
            assertEquals(46, response.usage?.outputTokens)
            assertEquals(117, response.usage?.totalTokens)
            assertEquals("stop", response.choices?.first()?.finishReason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.mistral.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"test-model",
                                "temperature":0.33,
                                "top_p":0.55,
                                "max_tokens":1000,
                                "stop":["bad word","stop word"],
                                "messages":[
                                    {"role":"user","content":"first question"},
                                    {"role":"assistant","content":"first answer"},
                                    {"role":"system","content":"Instructions"},
                                    {"role":"user","content":"Question"}
                                ],
                                "response_format":{"type":"json_object"}
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                MistralClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    MistralGenerateTextRequest
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
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }

    @Test
    fun genericGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.mistral.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"generic-model",
                                "temperature":0.66,
                                "top_p":0.77,
                                "max_tokens":2000,
                                "stop":["bad","stop"],
                                "messages":[
                                    {"role":"user","content":"Question A"},
                                    {"role":"assistant","content":"Answer A"},
                                    {"role":"system","content":"System Instructions"},
                                    {"role":"user","content":"Generic Question"}
                                ],
                                "response_format":{"type":"text"}
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                MistralClient
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
                    .expect(postRequestTo("https://api.mistral.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"default-model",
                                "temperature":0.66,
                                "messages":[
                                    {"role":"user","content":"Question"}
                                ]
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                MistralClient
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
