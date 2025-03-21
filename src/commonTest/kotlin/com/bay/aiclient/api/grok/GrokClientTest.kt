package com.bay.aiclient.api.grok

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

class GrokClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://api.x.ai/v1/models"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .andRespondOk(
                        """{
                          "data":[
                            {
                              "id": "grok-2-1212",
                              "created": 1737331200,
                              "object": "model",
                              "owned_by": "xai"
                            },
                            {
                              "id": "grok-2-vision-1212",
                              "created": 1733961600,
                              "object": "model",
                              "owned_by": "xai"
                            },
                            {
                              "id": "grok-beta",
                              "created": 1727136000,
                              "object": "model",
                              "owned_by": "xai"
                            },
                            {
                              "id": "grok-vision-beta",
                              "created": 1730764800,
                              "object": "model",
                              "owned_by": "xai"
                            },
                            {
                              "id": "grok-2-image-1212",
                              "created": 1736726400,
                              "object": "model",
                              "owned_by": "xai"
                            }
                          ],
                          "object":"list"
                        }
                        """.trimMargin(),
                    )

            val client =
                GrokClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels =
                listOf(
                    GrokModel("grok-2-1212", "grok-2-1212", 1737331200),
                    GrokModel("grok-2-vision-1212", "grok-2-vision-1212", 1733961600),
                    GrokModel("grok-beta", "grok-beta", 1727136000),
                    GrokModel("grok-vision-beta", "grok-vision-beta", 1730764800),
                    GrokModel("grok-2-image-1212", "grok-2-image-1212", 1736726400),
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
                    .expect(postRequestTo("https://api.x.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user","content":"Question"}
                                ],
                                "model":"test-model"
                            }
                            """.trimMargin(),
                        ),
                    ).andRespondOk(
                        """{
                            "id":"1f669a2b-3dd1-408f-8792-f42c7574ca57",
                            "object":"chat.completion",
                            "created":1742559097,
                            "model":"grok-2-1212",
                            "choices":[{
                                "index":0,
                                "message":{
                                   "role":"assistant",
                                   "content":"The answer to the Ultimate Question of Life, the Universe, and Everything is 42",
                                   "refusal":null
                                },
                                "finish_reason":"stop"
                            }],
                            "usage":{
                                "prompt_tokens":22,
                                "completion_tokens":85,
                                "reasoning_tokens":0,
                                "total_tokens":107,
                                "prompt_tokens_details":{
                                    "text_tokens":22,
                                    "audio_tokens":0,
                                    "image_tokens":0,
                                    "cached_tokens":0
                                }
                            },
                            "system_fingerprint":"fp_fe9e7ef66e"
                        }""",
                    )

            val client =
                GrokClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("The answer to the Ultimate Question of Life, the Universe, and Everything is 42", response.response)
            assertEquals(22, response.usage?.inputTokens)
            assertEquals(85, response.usage?.outputTokens)
            assertEquals(107, response.usage?.totalTokens)
            assertEquals("stop", response.choices?.first()?.finishReason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.x.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"system","content":"Instructions"},
                                    {"role":"user","content":"firstquestion"},
                                    {"role":"assistant","content":"firstanswer"},
                                    {"role":"user","content":"Question"}
                                ],
                                "model":"test-model",
                                "max_tokens":1000,
                                "response_format":{"type":"json_object"},
                                "stop":["badword","stopword"],
                                "temperature":0.33,
                                "top_p":0.55
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                GrokClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    GrokGenerateTextRequest
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
                    .expect(postRequestTo("https://api.x.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                   {"role":"system","content":"SystemInstructions"},
                                   {"role":"user","content":"QuestionA"},
                                   {"role":"assistant","content":"AnswerA"},
                                   {"role":"user","content":"GenericQuestion"}
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
                GrokClient
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
                    .expect(postRequestTo("https://api.x.ai/v1/chat/completions"))
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
                GrokClient
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
