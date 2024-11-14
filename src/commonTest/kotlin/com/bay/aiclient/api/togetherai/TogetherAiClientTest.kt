package com.bay.aiclient.api.togetherai

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

class TogetherAiClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://api.together.xyz/v1/models"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .andRespondOk(
                        """[
                            {
                                "id":"codellama/CodeLlama-34b-Instruct-hf",
                                "object":"model",
                                "created":1692898122,
                                "type":"chat",
                                "running":false,
                                "display_name":"Code Llama Instruct (34B)",
                                "organization":"Meta",
                                "license":"LLAMA 2 Community license Agreement (Meta)",
                                "context_length":16384,
                                "pricing": {
                                    "hourly": 0,
                                    "input": 0.3,
                                    "output": 0.3,
                                    "base": 0,
                                    "finetune": 0
                                }
                            },
                            {
                                "id":"upstage/SOLAR-10.7B-Instruct-v1.0",
                                "object":"model",
                                "created":1702851922,
                                "type":"chat",
                                "running":false,
                                "display_name":"Upstage SOLAR Instruct v1 (11B)",
                                "organization":"upstage",
                                "license":"cc-by-nc-4.0",
                                "context_length":4096
                            }
                        ]
                        """.trimMargin(),
                    )

            val client =
                TogetherAiClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels =
                listOf(
                    TogetherAiModel("codellama/CodeLlama-34b-Instruct-hf", "Code Llama Instruct (34B)", "chat", 1692898122),
                    TogetherAiModel("upstage/SOLAR-10.7B-Instruct-v1.0", "Upstage SOLAR Instruct v1 (11B)", "chat", 1702851922),
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
                    .expect(postRequestTo("https://api.together.xyz/v1/chat/completions"))
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
                            "id": "8e20a47468a86d7f-ABC",
                            "object": "chat.completion",
                            "created": 1731520628,
                            "model": "meta-llama/Meta-Llama-3-70B-Instruct-Turbo",
                            "prompt": [],
                            "choices": [
                                {
                                    "finish_reason": "eos",
                                    "seed": 15862945632254988000,
                                    "logprobs": null,
                                    "index": 0,
                                    "message": {
                                        "role": "assistant",
                                        "content": "The answer, of course, is 42.",
                                        "tool_calls": []
                                    }
                                }
                            ],
                            "usage": {
                                "prompt_tokens": 66,
                                "completion_tokens": 256,
                                "total_tokens": 322
                            }
                        }""",
                    )

            val client =
                TogetherAiClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("The answer, of course, is 42.", response.response)
            assertEquals(66, response.usage?.inputTokens)
            assertEquals(256, response.usage?.outputTokens)
            assertEquals(322, response.usage?.totalTokens)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.together.xyz/v1/chat/completions"))
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
                                "stop":["bad word","stop word"],
                                "temperature":0.33,
                                "top_p":0.55,
                                "top_k":5,
                                "repetition_penalty":0.3,
                                "presence_penalty":0.1,
                                "frequency_penalty":0.5,
                                "seed":10
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                TogetherAiClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    TogetherAiGenerateTextRequest
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
                            seed = 10
                            repetitionPenalty = 0.3
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
                    .expect(postRequestTo("https://api.together.xyz/v1/chat/completions"))
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
                                "stop":["bad","stop"],
                                "temperature":0.66,
                                "top_p":0.77
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                TogetherAiClient
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
                    .expect(postRequestTo("https://api.together.xyz/v1/chat/completions"))
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
                TogetherAiClient
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
