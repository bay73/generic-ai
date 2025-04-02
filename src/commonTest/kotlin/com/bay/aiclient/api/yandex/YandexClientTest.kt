package com.bay.aiclient.api.yandex

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

class YandexClientTest {
    @Test
    fun listModels_returnsStaticList() =
        runTest {
            val expectedModels =
                listOf(
                    YandexModel("yandexgpt-lite", "YandexGPT Lite"),
                    YandexModel("yandexgpt", "YandexGPT Pro"),
                    YandexModel("yandexgpt-32k", "YandexGPT Pro 32k"),
                    YandexModel("llama-lite", "Llama 8B"),
                    YandexModel("llama", "Llama 70B"),
                )
            val client = YandexClient.Builder().build()

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
                    .expect(postRequestTo("https://llm.api.cloud.yandex.net/foundationModels/v1/completion"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "modelUri":"gpt://fake-folder/test-model/latest",
                                "completionOptions": {
                                    "reasoningOptions": {
                                        "mode":"REASONING_MODE_UNSPECIFIED"
                                    }
                                },
                                "messages": [{"role":"user", "text":"Question"}],
                                "jsonObject":false
                            }""",
                        ),
                    ).andRespondOk(
                        """{
                            "alternatives": [{
                                "message": {
                                    "role": "assistant",
                                    "text": "Yandex Answer"
                                },
                                "status": "ALTERNATIVE_STATUS_FINAL"
                            }],
                            "usage": {
                                "inputTextTokens": "45",
                                "completionTokens": "15",
                                "totalTokens": "60",
                                "completionTokensDetails": {
                                    "reasoningTokens": "10"
                                }
                            },
                            "modelVersion": "latest"
                        }""",
                    )

            val client =
                YandexClient
                    .Builder()
                    .apply {
                        resourceFolder = "fake-folder"
                        apiKey = "fake_key"
                        defaultModel = "test-model"
                        httpEngine = mockEngine
                    }.build()

            val result = client.generateText { prompt = "Question" }

            assertTrue(result.isSuccess)
            val response = result.getOrThrow()
            assertEquals("Yandex Answer", response.response)
            assertEquals(45, response.usage?.inputTokens)
            assertEquals(15, response.usage?.outputTokens)
            assertEquals(60, response.usage?.totalTokens)
        }

    @Test
    fun fullGenerateTextRequest() =
        runTest {
            val mockEngine =
                MockHttpEngine()
                    .expect(postRequestTo("https://llm.api.cloud.yandex.net/foundationModels/v1/completion"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "modelUri":"gpt://fake-folder/test-model/latest",
                                "completionOptions": {
                                    "temperature":0.33,
                                    "maxTokens":1000,
                                    "reasoningOptions": {
                                        "mode":"REASONING_MODE_UNSPECIFIED"
                                    }
                                },
                                "messages": [
                                    {"role":"system","text":"Instructions"},
                                    {"role":"user","text":"firstquestion"},
                                    {"role":"assistant","text":"firstanswer"},
                                    {"role":"user","text":"Question"}
                                ],
                                "jsonObject":true
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                YandexClient
                    .Builder()
                    .apply {
                        resourceFolder = "fake-folder"
                        apiKey = "fake_key"
                        httpEngine = mockEngine
                    }.build()

            val result =
                client.generateText {
                    model = "test-model"
                    prompt = "Question"
                    systemInstructions = "Instructions"
                    responseFormat = ResponseFormat.JSON_OBJECT
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
                    .expect(postRequestTo("https://llm.api.cloud.yandex.net/foundationModels/v1/completion"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "modelUri":"gpt://fake-folder/generic-model/latest",
                                "completionOptions": {
                                    "temperature":0.66,
                                    "maxTokens":2000,
                                    "reasoningOptions": {
                                        "mode":"REASONING_MODE_UNSPECIFIED"
                                    }
                                },
                                "messages": [
                                    {"role":"system","text":"Instructions"},
                                    {"role":"user","text":"QuestionA"},
                                    {"role":"assistant","text":"AnswerA"},
                                    {"role":"user","text":"GenericQuestion"}
                                ],
                                "jsonObject":false
                            }""",
                        ),
                    ).andRespondOk("{ }")

            val client =
                YandexClient
                    .Builder()
                    .apply {
                        resourceFolder = "fake-folder"
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
                            systemInstructions = "Instructions"
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
                    .expect(postRequestTo("https://llm.api.cloud.yandex.net/foundationModels/v1/completion"))
                    .expect(header(HttpHeaders.Authorization, "Bearer fake_key"))
                    .expect(
                        jsonBody(
                            """{
                                "modelUri":"gpt://fake-folder/default-model/latest",
                                "completionOptions": {
                                    "temperature":0.66,
                                    "reasoningOptions": {
                                        "mode":"REASONING_MODE_UNSPECIFIED"
                                    }
                                },
                                "messages": [{"role":"user", "text":"Question"}],
                                "jsonObject":false
                            }""",
                        ),
                    ).andRespondOk("{}")

            val client =
                YandexClient
                    .Builder()
                    .apply {
                        resourceFolder = "fake-folder"
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
