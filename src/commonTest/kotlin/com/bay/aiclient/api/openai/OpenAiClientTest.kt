package com.bay.aiclient.api.openai

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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenAiClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://api.openai.com/v1/models"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .andRespondOk(
                        """{
                            "object": "list",
                            "data": [
                                {
                                    "id": "o1-preview",
                                    "object": "model",
                                    "created": 1725648897,
                                    "owned_by": "system"
                                },
                                {
                                    "id": "gpt-4",
                                    "object": "model",
                                    "created": 1687882411,
                                    "owned_by": "openai"
                                }
                            ]
                        }""",
                    )

            val client =
                OpenAiClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels =
                listOf(
                    OpenAiModel("o1-preview", "o1-preview", 1725648897),
                    OpenAiModel("gpt-4", "gpt-4", 1687882411),
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
                    .expect(postRequestTo("https://api.openai.com/v1/chat/completions"))
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
                OpenAiClient
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
                    .expect(postRequestTo("https://api.openai.com/v1/chat/completions"))
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
                                "max_completion_tokens":1000,
                                "presence_penalty":0.12,
                                "response_format":{"type":"json_object"},
                                "seed": 10,
                                "stop":["bad word","stop word"],
                                "temperature":0.33,
                                "top_p":0.55
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                OpenAiClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    OpenAiGenerateTextRequest
                        .builder()
                        .apply {
                            model = "test-model"
                            prompt = "Question"
                            systemInstructions = "Instructions"
                            responseFormat = ResponseFormat.JSON_OBJECT
                            chatHistory = listOf(TextMessage("user", "first question"), TextMessage("assistant", "first answer"))
                            maxOutputTokens = 1000
                            seed = 10
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
                    .expect(postRequestTo("https://api.openai.com/v1/chat/completions"))
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
                                "max_completion_tokens":2000,
                                "response_format":{"type":"text"},
                                "stop":["bad","stop"],
                                "temperature":0.66,
                                "top_p":0.77
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                OpenAiClient
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
                    .expect(postRequestTo("https://api.openai.com/v1/chat/completions"))
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
                OpenAiClient
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

    @Test
    fun responseSchema_passedToRequest() =
        runTest {
            val schema =
                JsonObject(
                    mapOf(
                        "type" to JsonPrimitive("object"),
                        "properties" to
                            JsonObject(
                                mapOf(
                                    "answers" to
                                        JsonObject(
                                            mapOf(
                                                "type" to JsonPrimitive("array"),
                                                "items" to
                                                    JsonObject(
                                                        mapOf(
                                                            "type" to JsonPrimitive("object"),
                                                            "properties" to
                                                                JsonObject(
                                                                    mapOf(
                                                                        "text" to
                                                                            JsonObject(
                                                                                mapOf(
                                                                                    "type" to JsonPrimitive("string"),
                                                                                ),
                                                                            ),
                                                                        "source" to
                                                                            JsonObject(
                                                                                mapOf(
                                                                                    "type" to JsonPrimitive("string"),
                                                                                ),
                                                                            ),
                                                                    ),
                                                                ),
                                                            "required" to
                                                                JsonArray(
                                                                    listOf(JsonPrimitive("text")),
                                                                ),
                                                        ),
                                                    ),
                                            ),
                                        ),
                                ),
                            ),
                        "required" to
                            JsonArray(
                                listOf(JsonPrimitive("answers")),
                            ),
                    ),
                )
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.openai.com/v1/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "messages":[
                                    {"role":"user","content":"Generic Question"}
                                ],
                                "model":"generic-model",
                                "response_format":{
                                    "type":"json_schema",
                                    "json_schema":{
                                        "name":"response_format",
                                        "description":null,
                                        "strict":false,
                                        "schema":{
                                            "type":"object",
                                            "properties":{
                                                "answers":{
                                                    "type":"array",
                                                    "items":{
                                                        "type":"object",
                                                        "properties":{
                                                            "text":{"type":"string"},
                                                            "source":{"type":"string"}
                                                        },
                                                        "required":["text"]
                                                    }
                                                }
                                            },
                                            "required":["answers"]
                                        }
                                    }
                                }
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                OpenAiClient
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
                            responseFormat = ResponseFormat.jsonSchema(schema)
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }
}
