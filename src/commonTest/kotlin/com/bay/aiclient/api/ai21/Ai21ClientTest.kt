package com.bay.aiclient.api.ai21

import com.bay.aiclient.domain.GenerateTextRequest
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

class Ai21ClientTest {
    @Test
    fun listModels_returnsStaticList() =
        runTest {
            val expectedModels = listOf(Ai21Model("jamba-1.5-large", "jamba-1.5-large"), Ai21Model("jamba-1.5-mini", "jamba-1.5-mini"))
            val client = Ai21Client.Builder().build()

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
                    .expect(postRequestTo("https://api.ai21.com/studio/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"test-model",
                                "messages":[{"role":"user", "content":"Question"}]
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "id": "msg_013Zva2CMHLNnXjNJJKqJ2EF",
                            "model": "jamba-1.5-large",
                            "choices":[{
                                "index":0,
                                "message":{"role":"assistant", "content":"This is Jamba Answer"},
                                "finish_reason":"stop"
                            }],
                            "usage":{
                                "prompt_tokens":132,
                                "completion_tokens":2056,
                                "total_tokens":2188
                            }
                        }""",
                    )

            val client =
                Ai21Client
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("This is Jamba Answer", response.response)
            assertEquals(132, response.usage?.inputTokens)
            assertEquals(2056, response.usage?.outputTokens)
            assertEquals(2188, response.usage?.totalTokens)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.ai21.com/studio/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"test-model",
                                "messages":[
                                    {"role":"user", "content":"first question"},
                                    {"role":"assistant", "content":"first answer"},
                                    {"role":"system", "content":"Instructions"},
                                    {"role":"user", "content":"Question"}
                                ],
                                "max_tokens":1000,
                                "temperature":0.33,
                                "stop":["bad word","stop word"]
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                Ai21Client
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText {
                    model = "test-model"
                    prompt = "Question"
                    systemInstructions = "Instructions"
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
                    .expect(postRequestTo("https://api.ai21.com/studio/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"generic-model",
                                "messages":[
                                    {"role":"user", "content":"Question A"},
                                    {"role":"assistant", "content":"Answer A"},
                                    {"role":"system", "content":"Instructions"},
                                    {"role":"user", "content":"Generic Question"}
                                ],
                                "max_tokens":2000,
                                "temperature":0.66,
                                "stop":["bad","stop"]
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                Ai21Client
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
                            systemInstructions = "Instructions"
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
                    .expect(postRequestTo("https://api.ai21.com/studio/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"default-model",
                                "messages":[{"role":"user", "content":"Question"}],
                                "temperature":0.66
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                Ai21Client
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
