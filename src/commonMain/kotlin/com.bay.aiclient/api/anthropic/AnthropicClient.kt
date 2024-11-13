package com.bay.aiclient.api.anthropic

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AnthropicClient internal constructor(
    apiAky: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<AnthropicModelsResponse> =
        Result.success(
            AnthropicModelsResponse(
                listOf(
                    AnthropicModel("claude-3-5-sonnet-20241022", "Claude 3.5 Sonnet"),
                    AnthropicModel("claude-3-5-haiku-20241022", "Claude 3.5 Haiku"),
                    AnthropicModel("claude-3-opus-20240229", "Claude 3 Opus"),
                    AnthropicModel("claude-3-sonnet-20240229", "Claude 3 Sonnet"),
                    AnthropicModel("claude-3-haiku-20240307", "Claude 3 Haiku"),
                ),
            ),
        )

    override suspend fun generateText(request: GenerateTextRequest): Result<AnthropicGenerateTextResponse> =
        generateText(request.toAnthropicRequest())

    suspend fun generateText(request: AnthropicGenerateTextRequest): Result<AnthropicGenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { AnthropicHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = mutableListOf<AnthropicHttpChatMessage>()
        newMessages.add(AnthropicHttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            AnthropicHttpChatRequest(
                model = request.model,
                messages = historyMessages + newMessages,
                max_tokens = request.maxOutputTokens,
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
        override var apiAky: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<AnthropicClient>() {
        override fun build(): AnthropicClient = AnthropicClient(apiAky, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.anthropic.com", timeout, httpLogLevel, httpEngine) {
            append("x-api-key", apiAky)
            append("anthropic-version", "2023-06-01")
        }
}
