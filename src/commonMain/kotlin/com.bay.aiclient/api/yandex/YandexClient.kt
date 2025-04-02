package com.bay.aiclient.api.yandex

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.ResponseFormat
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class YandexClient internal constructor(
    val resourceFolder: String,
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<YandexModelsResponse> =
        Result.success(
            YandexModelsResponse(
                listOf(
                    YandexModel("yandexgpt-lite", "YandexGPT Lite"),
                    YandexModel("yandexgpt", "YandexGPT Pro"),
                    YandexModel("yandexgpt-32k", "YandexGPT Pro 32k"),
                    YandexModel("llama-lite", "Llama 8B"),
                    YandexModel("llama", "Llama 70B"),
                ),
            ),
        )

    override suspend fun generateText(request: GenerateTextRequest): Result<YandexGenerateTextResponse> =
        generateText(request.toYandexRequest())

    suspend fun generateText(request: YandexGenerateTextRequest): Result<YandexGenerateTextResponse> {
        val modelUri =
            if (request.model.contains("/")) {
                "gpt://$resourceFolder/${request.model}"
            } else {
                "gpt://$resourceFolder/${request.model}/latest"
            }
        val systemMessages =
            if (request.systemInstructions?.isNotBlank() == true) {
                listOf(YandexHttpChatMessage(role = "system", request.systemInstructions))
            } else {
                emptyList()
            }
        val historyMessages = request.chatHistory?.map { YandexHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = listOf(YandexHttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            YandexHttpChatRequest(
                modelUri = modelUri,
                messages = systemMessages + historyMessages + newMessages,
                completionOptions =
                    YandexHttpCompletionOptions(
                        stream = false,
                        temperature = request.temperature,
                        maxTokens = request.maxOutputTokens,
                        reasoningOptions =
                            YandexHttpReasoningOptions(
                                mode = YandexHttpReasoningMode.REASONING_MODE_UNSPECIFIED,
                            ),
                    ),
                jsonObject = request.responseFormat == ResponseFormat.JSON_OBJECT,
                jsonSchema = request.responseFormat?.schema,
            )
        return client.runPost("/foundationModels/v1/completion", httpRequest) { result: YandexHttpChatResponse ->
            YandexGenerateTextResponse(
                response =
                    result.alternatives
                        ?.firstOrNull()
                        ?.message
                        ?.text ?: "",
                usage =
                    YandexGenerateTextTokenUsage(
                        inputTokens = result.usage?.inputTextTokens,
                        outputTokens = result.usage?.completionTokens,
                        totalTokens = result.usage?.totalTokens,
                    ),
                choices =
                    result.alternatives?.map {
                        YandexChatResponseChoice(
                            YandexChatMessage(role = it.message?.role, content = it.message?.text),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<YandexGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): YandexGenerateTextRequest.Builder {
        val builder = YandexGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toYandexRequest(): YandexGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        var resourceFolder: String = "",
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<YandexClient>() {
        override fun build(): YandexClient =
            YandexClient(resourceFolder, apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://llm.api.cloud.yandex.net", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiKey")
        }
}
