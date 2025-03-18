package com.bay.aiclient.api.anthropic

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AnthropicClient internal constructor(
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<AnthropicModelsResponse> =
        client.runGet("/v1/models") { result: AnthropicHttpModelsResponse ->
            AnthropicModelsResponse(
                result.data?.map { model -> AnthropicModel(id = model.id ?: "", name = model.display_name ?: "") }
                    ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<AnthropicGenerateTextResponse> =
        generateText(request.toAnthropicRequest())

    suspend fun generateText(request: AnthropicGenerateTextRequest): Result<AnthropicGenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { AnthropicHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = listOf(AnthropicHttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            AnthropicHttpChatRequest(
                model = request.model,
                messages = historyMessages + newMessages,
                max_tokens = request.maxOutputTokens ?: 4096,
                stop_sequences = request.stopSequences,
                system = request.systemInstructions,
                temperature = request.temperature,
                top_k = request.topK,
                top_p = request.topP,
            )
        return client.runPost("/v1/messages", httpRequest) { result: AnthropicHttpChatResponse ->
            AnthropicGenerateTextResponse(
                response =
                    result.content
                        ?.firstOrNull()
                        ?.text
                        ?: "",
                usage =
                    AnthropicGenerateTextTokenUsage(
                        inputTokens = result.usage?.input_tokens,
                        outputTokens = result.usage?.output_tokens,
                        totalTokens = (result.usage?.input_tokens ?: 0) + (result.usage?.output_tokens ?: 0),
                    ),
                finishReason = result.stop_reason,
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<AnthropicGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): AnthropicGenerateTextRequest.Builder {
        val builder = AnthropicGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toAnthropicRequest(): AnthropicGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<AnthropicClient>() {
        override fun build(): AnthropicClient = AnthropicClient(apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.anthropic.com", timeout, httpLogLevel, httpEngine) {
            append("x-api-key", apiKey)
            append("anthropic-version", "2023-06-01")
        }
}
