package com.bay.aiclient.api.inceptionlabs

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class InceptionLabsClient internal constructor(
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<InceptionLabsModelsResponse> =
        Result.success(
            InceptionLabsModelsResponse(listOf(InceptionLabsModel("mercury-coder-small", "mercury-coder-small"))),
        )

    override suspend fun generateText(request: GenerateTextRequest): Result<InceptionLabsGenerateTextResponse> =
        generateText(request.toInceptionLabsRequest())

    suspend fun generateText(request: InceptionLabsGenerateTextRequest): Result<InceptionLabsGenerateTextResponse> {
        val systemMessages =
            if (request.systemInstructions?.isNotBlank() == true) {
                listOf(InceptionLabsHttpChatMessage(role = "system", request.systemInstructions))
            } else {
                emptyList()
            }
        val historyMessages = request.chatHistory?.map { InceptionLabsHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = listOf(InceptionLabsHttpChatMessage(role = "user", request.prompt))

        val httpRequest =
            InceptionLabsHttpChatRequest(
                messages = systemMessages + historyMessages + newMessages,
                model = request.model,
                frequency_penalty = request.frequencyPenalty,
                max_tokens = request.maxOutputTokens,
                presence_penalty = request.presencePenalty,
                stop = request.stopSequences,
                stream = false,
                temperature = request.temperature,
                top_p = request.topP,
            )
        return client.runPost("/v1/chat/completions", httpRequest) { result: InceptionLabsHttpChatResponse ->
            InceptionLabsGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    InceptionLabsGenerateTextTokenUsage(
                        inputTokens = result.usage?.prompt_tokens,
                        outputTokens = result.usage?.completion_tokens,
                        totalTokens = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        InceptionLabsChatResponseChoice(
                            finishReason = it.finish_reason,
                            index = it.index,
                            InceptionLabsChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<InceptionLabsGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): InceptionLabsGenerateTextRequest.Builder {
        val builder = InceptionLabsGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toInceptionLabsRequest(): InceptionLabsGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<InceptionLabsClient>() {
        override fun build(): InceptionLabsClient =
            InceptionLabsClient(apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.inceptionlabs.ai/", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiKey")
        }
}
