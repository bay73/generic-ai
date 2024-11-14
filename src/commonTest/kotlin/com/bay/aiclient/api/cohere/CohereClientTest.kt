package com.bay.aiclient.api.cohere

import com.bay.aiclient.domain.GenerateTextRequest
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

class CohereClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://api.cohere.com/v1/models?page_size=1000"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .andRespondOk(
                        """{
                            "models": [
                                {
                                    "name": "name",
                                    "endpoints": [
                                        "chat"
                                    ],
                                    "finetuned": true,
                                    "context_length": 1.1,
                                    "tokenizer_url": "tokenizer_url",
                                    "default_endpoints": [
                                        "chat"
                                    ]
                                }
                            ],
                            "next_page_token": "next_page_token"
                        }""",
                    )

            val client =
                CohereClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels = listOf(CohereModel("name", "name"))

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
                    .expect(postRequestTo("https://api.cohere.com/v1/chat"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "message":"Question",
                                "model":"test-model",
                                "chat_history":[]
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "text": "The man who is widely credited with discovering gravity is Sir Isaac Newton",
                            "generation_id": "0385c7cf-4247-43a3-a450-b25b547a31e1",
                            "finish_reason": "COMPLETE",
                            "chat_history": [
                                {
                                    "role": "USER",
                                    "message": "Who discovered gravity?"
                                },
                                {
                                    "role": "CHATBOT",
                                    "message": "The man who is widely credited with discovering gravity is Sir Isaac Newton"
                                }
                            ],
                            "meta": {
                                "api_version": {
                                    "version": "1"
                                },
                                "billed_units": {
                                    "input_tokens": 31,
                                    "output_tokens": 35
                                },
                                "tokens": {
                                    "input_tokens": 31,
                                    "output_tokens": 205
                                }
                            }
                        }""",
                    )

            val client =
                CohereClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("The man who is widely credited with discovering gravity is Sir Isaac Newton", response.response)
            assertEquals(31, response.usage?.inputTokens)
            assertEquals(205, response.usage?.outputTokens)
            assertEquals(236, response.usage?.totalTokens)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.cohere.com/v1/chat"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "message":"Question",
                                "model":"test-model",
                                "preamble":"Instructions",
                                "chat_history":[
                                    {"role":"user","message":"first question"},
                                    {"role":"assistant","message":"first answer"}
                                ],
                                "temperature":0.33,
                                "max_tokens":1000,
                                "max_input_tokens":200,
                                "k":5,
                                "p":0.55,
                                "seed":10,
                                "stop_sequences":["bad word","stop word"],
                                "frequency_penalty":0.5,
                                "presence_penalty":0.1
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                CohereClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    CohereGenerateTextRequest
                        .builder()
                        .apply {
                            model = "test-model"
                            prompt = "Question"
                            systemInstructions = "Instructions"
                            chatHistory = listOf(TextMessage("user", "first question"), TextMessage("assistant", "first answer"))
                            maxOutputTokens = 1000
                            stopSequences = listOf("bad word", "stop word")
                            temperature = 0.33
                            topP = 0.55
                            topK = 5
                            maxInputTokens = 200
                            seed = 10
                            frequencyPenalty = 0.5
                            presencePenalty = 0.1
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }

    @Test
    fun genericGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.cohere.com/v1/chat"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "message":"Generic Question",
                                "model":"generic-model",
                                "preamble":"System Instructions",
                                "chat_history":[
                                    {"role":"user","message":"Question A"},
                                    {"role":"assistant","message":"Answer A"}],
                                "temperature":0.66,
                                "max_tokens":2000,
                                "p":0.77,
                                "stop_sequences":["bad","stop"]
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                CohereClient
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
                    .expect(postRequestTo("https://api.cohere.com/v1/chat"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "message":"Question",
                                "model":"default-model",
                                "chat_history":[],
                                "temperature":0.66
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                CohereClient
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
