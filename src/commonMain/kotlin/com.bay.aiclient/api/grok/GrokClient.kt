package com.bay.aiclient.api.grok

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class GrokClient internal constructor(
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<GrokModelsResponse> =
        client.runGet("/v1/models") { result: GrokHttpModelsResponse ->
            GrokModelsResponse(
                result.data?.map { model -> GrokModel(id = model.id ?: "", name = model.id ?: "", created = model.created) } ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<GrokGenerateTextResponse> =
        generateText(request.toGrokRequest())

    suspend fun generateText(request: GrokGenerateTextRequest): Result<GrokGenerateTextResponse> {
        val systemMessages =
            if (request.systemInstructions?.isNotBlank() == true) {
                listOf(GrokHttpChatMessage(role = "system", request.systemInstructions))
            } else {
                emptyList()
            }
        val historyMessages = request.chatHistory?.map { GrokHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = listOf(GrokHttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            GrokHttpChatRequest(
                messages = systemMessages + historyMessages + newMessages,
                model = request.model,
                deferred = false,
                frequency_penalty = request.frequencyPenalty,
                max_tokens = request.maxOutputTokens,
                presence_penalty = request.presencePenalty,
                response_format = GrokHttpChatResponseFormat.from(request.responseFormat),
                seed = request.seed,
                stop = request.stopSequences,
                temperature = request.temperature,
                top_p = request.topP,
            )
        return client.runPost("/v1/chat/completions", httpRequest) { result: GrokHttpChatResponse ->
            GrokGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    GrokGenerateTextTokenUsage(
                        inputTokens = result.usage?.prompt_tokens,
                        outputTokens = result.usage?.completion_tokens,
                        totalTokens = result.usage?.total_tokens,
                        reasoningTokens = result.usage?.reasoning_tokens,
                    ),
                choices =
                    result.choices?.map {
                        GrokChatResponseChoice(
                            finishReason = it.finish_reason,
                            index = it.index,
                            GrokChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<GrokGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): GrokGenerateTextRequest.Builder {
        val builder = GrokGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toGrokRequest(): GrokGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<GrokClient>() {
        override fun build(): GrokClient = GrokClient(apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.x.ai", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiKey")
        }
}
