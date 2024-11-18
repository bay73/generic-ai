package com.bay.aiclient.api.sambanova

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

class SambaNovaClientTest {
    @Test
    fun listModels_returnsStaticList() =
        runTest {
            val expectedModels =
                listOf(
                    SambaNovaModel("Meta-Llama-3.2-1B-Instruct", "Meta-Llama-3.2-1B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.2-3B-Instruct", "Meta-Llama-3.2-3B-Instruct"),
                    SambaNovaModel("Llama-3.2-11B-Vision-Instruct", "Llama-3.2-11B-Vision-Instruct"),
                    SambaNovaModel("Llama-3.2-90B-Vision-Instruct", "Llama-3.2-90B-Vision-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-8B-Instruct", "Meta-Llama-3.1-8B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-70B-Instruct", "Meta-Llama-3.1-70B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-405B-Instruct", "Meta-Llama-3.1-405B-Instruct"),
                )
            val client = SambaNovaClient.Builder().build()

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
                    .expect(postRequestTo("https://api.sambanova.ai/v1/chat/completions"))
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
                            "id": "chatcmpl-123",
                            "object": "chat.completion",
                            "created": 1677652288,
                            "model": "Llama-3-8b-chat",
                            "choices": [{
                                "index": 0,
                                "message": {
                                    "role": "assistant",
                                    "content": "Hello there, how may I assist you today?"
                                },
                                "logprobs": null,
                                "finish_reason": "stop"
                            }],
                            "usage":{
                                "completion_tokens":136,
                                "completion_tokens_after_first_per_sec":1478.2479205935642,
                                "completion_tokens_after_first_per_sec_first_ten":1529.0379497648646,
                                "completion_tokens_per_sec":1211.186723401364,
                                "end_time":1731520628.1027606,
                                "is_last_response":true,
                                "prompt_tokens":90,
                                "start_time":1731520627.990474,
                                "time_to_first_token":0.020962238311767578,
                                "total_latency":0.11228656768798828,
                                "total_tokens":226,
                                "total_tokens_per_sec":2012.7073491816784
                            }
                        }""",
                    )

            val client =
                SambaNovaClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("Hello there, how may I assist you today?", response.response)
            assertEquals(90, response.usage?.inputTokens)
            assertEquals(136, response.usage?.outputTokens)
            assertEquals(226, response.usage?.totalTokens)
            assertEquals("stop", response.choices?.first()?.finishReason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.sambanova.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user","content":"first question"},
                                    {"role":"assistant","content":"first answer"},
                                    {"role":"system","content":"Instructions"},
                                    {"role":"user","content":"Question"}
                                ],
                                "model":"test-model",
                                "max_tokens":1000,
                                "temperature":0.33,
                                "top_p":0.55,
                                "top_k":5,
                                "stop":["bad word","stop word"]
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                SambaNovaClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    SambaNovaGenerateTextRequest
                        .builder()
                        .apply {
                            model = "test-model"
                            prompt = "Question"
                            systemInstructions = "Instructions"
                            responseFormat = ResponseFormat.JSON_OBJECT // This going to be ignored as only text is supported
                            chatHistory = listOf(TextMessage("user", "first question"), TextMessage("assistant", "first answer"))
                            maxOutputTokens = 1000
                            stopSequences = listOf("bad word", "stop word")
                            temperature = 0.33
                            topP = 0.55
                            topK = 5
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }

    @Test
    fun genericGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.sambanova.ai/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user","content":"Question A"},
                                    {"role":"assistant","content":"Answer A"},
                                    {"role":"system","content":"System Instructions"},
                                    {"role":"user","content":"Generic Question"}
                                ],
                                "model":"generic-model",
                                "max_tokens":2000,
                                "temperature":0.66,
                                "top_p":0.77,
                                "stop":["bad","stop"]
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                SambaNovaClient
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
                    .expect(postRequestTo("https://api.sambanova.ai/v1/chat/completions"))
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
                SambaNovaClient
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
