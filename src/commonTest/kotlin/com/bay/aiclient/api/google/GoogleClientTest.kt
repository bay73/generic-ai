package com.bay.aiclient.api.google

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.TextMessage
import com.bay.aiclient.utils.MockHttpEngine
import com.bay.aiclient.utils.RequestMatcher.Companion.getRequestTo
import com.bay.aiclient.utils.RequestMatcher.Companion.header
import com.bay.aiclient.utils.RequestMatcher.Companion.jsonBody
import com.bay.aiclient.utils.RequestMatcher.Companion.postRequestTo
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoogleClientTest {
    @Test
    fun listModels_parsesResponse() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(getRequestTo("https://generativelanguage.googleapis.com/v1beta/models"))
                    .expect(header("x-goog-api-key", "fake_key"))
                    .andRespondOk(
                        """{
                            "models": [
                                {
                                    "name": "models/gemini-1.0-pro",
                                    "version": "001",
                                    "displayName": "Gemini 1.0 Pro",
                                    "description": "The best model for scaling across a wide range of tasks",
                                    "inputTokenLimit": 30720,
                                    "outputTokenLimit": 2048,
                                    "supportedGenerationMethods": [
                                        "generateContent",
                                        "countTokens"
                                    ],
                                    "temperature": 0.9,
                                    "topP": 1
                                },
                                {
                                    "name": "models/gemini-1.5-pro-latest",
                                    "version": "001",
                                    "displayName": "Gemini 1.5 Pro Latest",
                                    "description": "Mid-size multimodal model that supports up to 2 million tokens",
                                    "inputTokenLimit": 2000000,
                                    "outputTokenLimit": 8192,
                                    "supportedGenerationMethods": [
                                        "generateContent",
                                        "countTokens"
                                    ],
                                    "temperature": 1,
                                    "topP": 0.95,
                                    "topK": 40,
                                    "maxTemperature": 2
                                }
                            ],
                            "nextPageToken": string
                        }""",
                    )

            val client =
                GoogleClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val expectedModels =
                listOf(
                    GoogleModel("models/gemini-1.0-pro", "Gemini 1.0 Pro"),
                    GoogleModel("models/gemini-1.5-pro-latest", "Gemini 1.5 Pro Latest"),
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
                    .expect(postRequestTo("https://generativelanguage.googleapis.com/v1beta/test-model:generateContent"))
                    .expect(header("x-goog-api-key", "fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "contents":[
                                    {"role":"user","parts":[{"text":"Question"}]}
                                ],
                                "generationConfig":{}
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "candidates": [
                                {
                                    "content": {
                                        "parts": [{
                                            "text": "The answer to the Ultimate Question of Life, the Universe, and Everything is **42**"
                                        }],
                                        "role": "model"
                                    },
                                    "finishReason": "STOP",
                                    "index": 0,
                                    "safetyRatings": [
                                        {
                                            "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                                            "probability": "NEGLIGIBLE"
                                        },
                                        {
                                            "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                                            "probability": "NEGLIGIBLE"
                                        }
                                    ]
                                }
                            ],
                            "usageMetadata": {
                                "promptTokenCount": 117,
                                "candidatesTokenCount": 85,
                                "totalTokenCount": 202
                            },
                            "modelVersion": "gemini-1.5-pro-001"
                        }""",
                    )

            val client =
                GoogleClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("The answer to the Ultimate Question of Life, the Universe, and Everything is **42**", response.response)
            assertEquals(117, response.usage?.inputTokens)
            assertEquals(85, response.usage?.outputTokens)
            assertEquals(202, response.usage?.totalTokens)
            assertEquals("STOP", response.candidates?.first()?.finishReason)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://generativelanguage.googleapis.com/v1beta/test-model:generateContent"))
                    .expect(header("x-goog-api-key", "fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "contents":[
                                    {"role":"user","parts":[{"text":"first question"}]},
                                    {"role":"assistant","parts":[{"text":"first answer"}]},
                                    {"role":"user","parts":[{"text":"Question"}]}
                                ],
                                "systemInstruction":{"parts":[{"text":"Instructions"}]},
                                "generationConfig":{
                                    "stopSequences":["bad word","stop word"],
                                    "maxOutputTokens":1000,
                                    "temperature":0.33,
                                    "topP":0.55,
                                    "topK":5,
                                    "presencePenalty":0.1,
                                    "frequencyPenalty":0.5
                                }
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                GoogleClient
                    .Builder()
                    .apply {
                        apiAky = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText(
                    GoogleGenerateTextRequest
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
                            presencePenalty = 0.1
                            frequencyPenalty = 0.5
                        }.build(),
                )

            assertTrue(result.isSuccess)
        }

    @Test
    fun genericGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://generativelanguage.googleapis.com/v1beta/generic-model:generateContent"))
                    .expect(header("x-goog-api-key", "fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "contents":[
                                    {"role":"user","parts":[{"text":"Question A"}]},
                                    {"role":"assistant","parts":[{"text":"Answer A"}]},
                                    {"role":"user","parts":[{"text":"Generic Question"}]}
                                ],
                                "systemInstruction":{"parts":[{"text":"System Instructions"}]},
                                "generationConfig":{
                                    "stopSequences":["bad","stop"],
                                    "maxOutputTokens":2000,
                                    "temperature":0.66,
                                    "topP":0.77
                                }
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                GoogleClient
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
                    .expect(postRequestTo("https://generativelanguage.googleapis.com/v1beta/default-model:generateContent"))
                    .expect(header("x-goog-api-key", "fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "contents":[
                                    {"role":"user","parts":[{"text":"Question"}]}
                                ],
                                "generationConfig":{
                                    "temperature":0.66
                                }
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                GoogleClient
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