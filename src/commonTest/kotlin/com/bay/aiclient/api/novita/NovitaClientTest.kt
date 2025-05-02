package com.bay.aiclient.api.novita

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

class NovitaClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://api.novita.ai/v3/openai/models"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .andRespondOk(
                        """{
                            "data": [
                                {
                                    "created":1746010994,
                                    "id":"deepseek/deepseek-prover-v2-671b",
                                    "object":"model",
                                    "owned_by":"unknown",
                                    "permission":null,
                                    "root":"",
                                    "parent":"",
                                    "input_token_price_per_m":7000,
                                    "output_token_price_per_m":25000,
                                    "title":"deepseek/deepseek-prover-v2-671b",
                                    "description":"DeepSeek Launches Open-Source Model DeepSeek-Prover-V2-671B, Specializing in Mathematical Theorem Proving.",
                                    "tags":["NEW"],
                                    "context_size":160000,
                                    "status":1,
                                    "display_name":"Deepseek Prover V2 671B",
                                    "model_type":"chat",
                                    "max_output_tokens":160000
                                },{
                                    "created":1745897024,
                                    "id":"qwen/qwen3-235b-a22b-fp8",
                                    "object":"model",
                                    "owned_by":"unknown",
                                    "permission":null,
                                    "root":"",
                                    "parent":"",
                                    "input_token_price_per_m":2000,
                                    "output_token_price_per_m":8000,
                                    "title":"qwen/qwen3-235b-a22b-fp8",
                                    "description":"Achieves effective integration of inference and non-inference modes.",
                                    "tags":["NEW"],
                                    "context_size":128000,
                                    "status":1,
                                    "display_name":"Qwen3-235B-A22B",
                                    "model_type":"chat",
                                    "max_output_tokens":128000
                                }
                            ]
                        }""",
                    )

            val client =
                NovitaClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels =
                listOf(
                    NovitaModel(
                        "deepseek/deepseek-prover-v2-671b",
                        "Deepseek Prover V2 671B",
                        1746010994,
                        "DeepSeek Launches Open-Source Model DeepSeek-Prover-V2-671B, Specializing in Mathematical Theorem Proving.",
                        160000,
                    ),
                    NovitaModel(
                        "qwen/qwen3-235b-a22b-fp8",
                        "Qwen3-235B-A22B",
                        1745897024,
                        "Achieves effective integration of inference and non-inference modes.",
                        128000,
                    ),
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
                    .expect(postRequestTo("https://api.novita.ai/v3/openai/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"test-model",
                                "messages":[
                                    {"role":"user","content":"Question"}
                                ]
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "id":"02174619591940613bec6323f33f78c303ae4a7b399329b7618d5",
                            "object":"chat.completion",
                            "created":1746195922,
                            "model":"deepseek/deepseek-v3-turbo",
                            "choices":[{
                                "index":0,
                                "message":{
                                    "role":"assistant",
                                    "content":"The answer to the Ultimate Question of Life, the Universe, and Everything is **42**."
                                },
                                "finish_reason":"stop",
                                "content_filter_results":{
                                    "hate":{"filtered":false},
                                    "self_harm":{"filtered":false},
                                    "sexual":{"filtered":false},
                                    "violence":{"filtered":false},
                                    "jailbreak":{"filtered":false,"detected":false},
                                    "profanity":{"filtered":false,"detected":false}
                                }
                            }],
                            "usage":{
                                "prompt_tokens":16,
                                "completion_tokens":86,
                                "total_tokens":102,
                                "prompt_tokens_details":{"audio_tokens":0,"cached_tokens":0},
                                "completion_tokens_details":{"audio_tokens":0,"reasoning_tokens":0}
                            },
                            "system_fingerprint":""
                        }""",
                    )

            val client =
                NovitaClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("The answer to the Ultimate Question of Life, the Universe, and Everything is **42**.", response.response)
            assertEquals(16, response.usage?.inputTokens)
            assertEquals(86, response.usage?.outputTokens)
            assertEquals(102, response.usage?.totalTokens)
            assertEquals("stop", response.choices?.first()?.finishReason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.novita.ai/v3/openai/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"test-model",
                                "messages":[
                                    {"role":"system","content":"Instructions"},
                                    {"role":"user","content":"first question"},
                                    {"role":"assistant","content":"first answer"},
                                    {"role":"user","content":"Question"}
                                ],
                                "max_tokens":1000,
                                "seed": 10,
                                "frequency_penalty":0.67,
                                "presence_penalty":0.12,
                                "repetition_penalty":0.18,
                                "stop":["badword","stopword"],
                                "temperature":0.33,
                                "top_p":0.55,
                                "min_p":0.01,
                                "response_format":{"type":"json_object"}
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                NovitaClient
                    .Builder()
                    .apply {
                        apiKey = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    NovitaGenerateTextRequest.Companion
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
                            repetitionPenalty = 0.18
                            minP = 0.01
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }

    @Test
    fun genericGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://api.novita.ai/v3/openai/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"generic-model",
                                "messages":[
                                    {"role":"system","content":"SystemInstructions"},
                                    {"role":"user","content":"QuestionA"},
                                    {"role":"assistant","content":"AnswerA"},
                                    {"role":"user","content":"GenericQuestion"}
                                ],
                                "max_tokens":2000,
                                "stop":["bad","stop"],
                                "temperature":0.66,
                                "top_p":0.77,
                                "response_format":{"type":"text"}
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                NovitaClient
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
                    .expect(postRequestTo("https://api.novita.ai/v3/openai/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"default-model",
                                "messages":[
                                    {"role":"user","content":"Question"}
                                ],
                                "temperature":0.66
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                NovitaClient
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
                    .expect(postRequestTo("https://api.novita.ai/v3/openai/chat/completions"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "model":"generic-model",
                                "messages":[
                                    {"role":"user","content":"Generic Question"}
                                ],
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
                NovitaClient
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
