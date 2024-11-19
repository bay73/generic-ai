package com.bay.aiclient.api.cohere

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CohereClient internal constructor(
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<CohereModelsResponse> =
        client.runGet("/v1/models?page_size=1000") { result: CohereHttpModelsResponse ->
            CohereModelsResponse(result.models?.map { model -> CohereModel(id = model.name ?: "", name = model.name ?: "") } ?: emptyList())
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<CohereGenerateTextResponse> =
        generateText(request.toCohereRequest())

    suspend fun generateText(request: CohereGenerateTextRequest): Result<CohereGenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { CohereHttpChatMessage(it.role, it.content) } ?: emptyList()
        val httpRequest =
            CohereHttpChatRequest(
                message = request.prompt,
                model = request.model,
                preamble = request.systemInstructions,
                response_format = CohereHttpChatResponseFormat.from(request.responseFormat),
                chat_history = historyMessages,
                max_tokens = request.maxOutputTokens,
                temperature = request.temperature,
                max_input_tokens = request.maxInputTokens,
                k = request.topK,
                p = request.topP,
                seed = request.seed,
                stop_sequences = request.stopSequences,
                frequency_penalty = request.frequencyPenalty,
                presence_penalty = request.presencePenalty,
            )
        return client.runPost("/v1/chat", httpRequest) { result: CohereHttpChatResponse ->
            CohereGenerateTextResponse(
                response = result.text,
                usage =
                    CohereGenerateTextTokenUsage(
                        inputTokens = result.meta?.tokens?.input_tokens,
                        outputTokens = result.meta?.tokens?.output_tokens,
                        totalTokens = (result.meta?.tokens?.input_tokens ?: 0) + (result.meta?.tokens?.output_tokens ?: 0),
                    ),
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<CohereGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): CohereGenerateTextRequest.Builder {
        val builder = CohereGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toCohereRequest(): CohereGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<CohereClient>() {
        override fun build(): CohereClient = CohereClient(apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.cohere.com", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiKey")
        }
}
