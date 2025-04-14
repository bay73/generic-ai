package com.bay.aiclient.api.inceptionlabs

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.ResponseFormat
import com.bay.aiclient.domain.TextMessage
import com.bay.aiclient.utils.MockHttpEngine
import com.bay.aiclient.utils.RequestMatcher.Companion.header
import com.bay.aiclient.utils.RequestMatcher.Companion.jsonBody
import com.bay.aiclient.utils.RequestMatcher.Companion.postRequestTo
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InceptionLabsClientTest {
    @Test
    fun listModels_returnsStaticList() =
        runTest {
            val client = InceptionLabsClient.Builder().build()

            val expectedModels = listOf(InceptionLabsModel("mercury-coder-small", "mercury-coder-small"))

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
                    .expect(postRequestTo("https://api.inceptionlabs.ai/v1/chat/completions"))
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
                            "id":"81a65098-5abc-405c-a811-6090317b8d5f",
                            "object":"chat.completion",
                            "created":1744624788,
                            "model":"mercury-coder-small",
                            "choices":[
                                {
                                    "index":0,
                                    "message":{
                                         "role":"assistant",
                                         "content":"The answer to the Ultimate Question of Life is famously known to be the number 42.",
                                         "tool_calls":[]
                                    },
                                    "finish_reason":"stop"
                                }
                            ],
                            "usage":{
                                "prompt_tokens":153,
                                "completion_tokens":5967,
                                "total_tokens":6120
                            }
                        }""",
                    )

            val client =
                InceptionLabsClient
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
            assertEquals(153, response.usage?.inputTokens)
            assertEquals(5967, response.usage?.outputTokens)
            assertEquals(6120, response.usage?.totalTokens)
            assertEquals("stop", response.choices?.first()?.finishReason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.inceptionlabs.ai/v1/chat/completions"))
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
                                "temperature":0.33,
                                "max_tokens":1000,
                                "top_p":0.55,
                                "frequency_penalty":0.67,
                                "presence_penalty":0.12,
                                "stop":["bad word","stop word"]
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                InceptionLabsClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    InceptionLabsGenerateTextRequest
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
                    .expect(postRequestTo("https://api.inceptionlabs.ai/v1/chat/completions"))
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
                                "temperature":0.66,
                                "max_tokens":2000,
                                "top_p":0.77,
                                "stop":["bad","stop"]
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                InceptionLabsClient
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
                    .expect(postRequestTo("https://api.inceptionlabs.ai/v1/chat/completions"))
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
                InceptionLabsClient
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
