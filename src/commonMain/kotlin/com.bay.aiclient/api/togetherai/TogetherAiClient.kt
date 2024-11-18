package com.bay.aiclient.api.togetherai

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TogetherAiClient internal constructor(
    apiAky: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<TogetherAiModelsResponse> =
        client.runGet("/v1/models") { result: List<TogetherAiHttpModel> ->
            TogetherAiModelsResponse(
                result.map { model ->
                    TogetherAiModel(id = model.id ?: "", name = model.display_name ?: "", type = model.type, created = model.created)
                }
                    ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<TogetherAiGenerateTextResponse> =
        generateText(request.toTogetherAiRequest())

    suspend fun generateText(request: TogetherAiGenerateTextRequest): Result<TogetherAiGenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { TogetherAiHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = mutableListOf<TogetherAiHttpChatMessage>()
        if (request.systemInstructions?.isNotBlank() == true) {
            newMessages.add(TogetherAiHttpChatMessage(role = "system", request.systemInstructions))
        }
        newMessages.add(TogetherAiHttpChatMessage(role = "user", request.prompt))

        val httpRequest =
            TogetherAiHttpChatRequest(
                messages = historyMessages + newMessages,
                model = request.model,
                max_tokens = request.maxOutputTokens,
                stop = request.stopSequences,
                temperature = request.temperature,
                top_p = request.topP,
                top_k = request.topK,
                repetition_penalty = request.repetitionPenalty,
                frequency_penalty = request.frequencyPenalty,
                presence_penalty = request.presencePenalty,
                seed = request.seed,
                response_format = TogetherAiHttpChatResponseFormat.from(request.responseFormat),
            )
        return client.runPost("/v1/chat/completions", httpRequest) { result: TogetherAiHttpChatResponse ->
            TogetherAiGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?: "",
                usage =
                    TogetherAiGenerateTextTokenUsage(
                        inputTokens = result.usage?.prompt_tokens,
                        outputTokens = result.usage?.completion_tokens,
                        totalTokens = result.usage?.total_tokens,
                    ),
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<TogetherAiGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): TogetherAiGenerateTextRequest.Builder {
        val builder = TogetherAiGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toTogetherAiRequest(): TogetherAiGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiAky: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<TogetherAiClient>() {
        override fun build(): TogetherAiClient =
            TogetherAiClient(apiAky, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.together.xyz", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiAky")
        }
}
