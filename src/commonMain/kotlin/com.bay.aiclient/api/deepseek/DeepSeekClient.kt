package com.bay.aiclient.api.deepseek

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DeepSeekClient internal constructor(
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<DeepSeekModelsResponse> =
        client.runGet("/models") { result: DeepSeekHttpModelsResponse ->
            DeepSeekModelsResponse(
                result.data?.map { model -> DeepSeekModel(id = model.id ?: "", name = model.id ?: "") }
                    ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<DeepSeekGenerateTextResponse> =
        generateText(request.toDeepSeekRequest())

    suspend fun generateText(request: DeepSeekGenerateTextRequest): Result<DeepSeekGenerateTextResponse> {
        val systemMessages =
            if (request.systemInstructions?.isNotBlank() == true) {
                listOf(DeepSeekHttpChatMessage(role = "system", request.systemInstructions))
            } else {
                emptyList()
            }
        val historyMessages = request.chatHistory?.map { DeepSeekHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = listOf(DeepSeekHttpChatMessage(role = "user", request.prompt))

        val httpRequest =
            DeepSeekHttpChatRequest(
                messages = systemMessages + historyMessages + newMessages,
                model = request.model,
                frequency_penalty = request.frequencyPenalty,
                max_tokens = request.maxOutputTokens,
                presence_penalty = request.presencePenalty,
                response_format = DeepSeekHttpChatResponseFormat.from(request.responseFormat),
                stop = request.stopSequences,
                stream = false,
                temperature = request.temperature,
                top_p = request.topP,
            )
        return client.runPost("/chat/completions", httpRequest) { result: DeepSeekHttpChatResponse ->
            DeepSeekGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    DeepSeekGenerateTextTokenUsage(
                        inputTokens = result.usage?.prompt_tokens,
                        outputTokens = result.usage?.completion_tokens,
                        totalTokens = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        DeepSeekChatResponseChoice(
                            finishReason = it.finish_reason,
                            index = it.index,
                            DeepSeekChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<DeepSeekGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): DeepSeekGenerateTextRequest.Builder {
        val builder = DeepSeekGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toDeepSeekRequest(): DeepSeekGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<DeepSeekClient>() {
        override fun build(): DeepSeekClient = DeepSeekClient(apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.deepseek.com/", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiKey")
        }
}
