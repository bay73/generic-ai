package com.bay.aiclient.api.openai

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class OpenAiClient internal constructor(
    apiAky: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<OpenAiModelsResponse> =
        client.runGet("/v1/models") { result: OpenAiHttpModelsResponse ->
            OpenAiModelsResponse(
                result.data?.map { model -> OpenAiModel(id = model.id ?: "", name = model.id ?: "", created = model.created) }
                    ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<OpenAiGenerateTextResponse> =
        generateText(request.toOpenAiRequest())

    suspend fun generateText(request: OpenAiGenerateTextRequest): Result<OpenAiGenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { OpenAiHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = mutableListOf<OpenAiHttpChatMessage>()
        if (request.systemInstructions?.isNotBlank() == true) {
            newMessages.add(OpenAiHttpChatMessage(role = "system", request.systemInstructions))
        }
        newMessages.add(OpenAiHttpChatMessage(role = "user", request.prompt))

        val httpRequest =
            OpenAiHttpChatRequest(
                messages = historyMessages + newMessages,
                model = request.model,
                frequency_penalty = request.frequencyPenalty,
                max_completion_tokens = request.maxOutputTokens,
                presence_penalty = request.presencePenalty,
                response_format = OpenAiHttpChatResponseFormat.from(request.responseFormat),
                seed = request.seed,
                stop = request.stopSequences,
                stream = false,
                temperature = request.temperature,
                top_p = request.topP,
            )
        return client.runPost("/v1/chat/completions", httpRequest) { result: OpenAiHttpChatResponse ->
            OpenAiGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    OpenAiGenerateTextTokenUsage(
                        inputTokens = result.usage?.prompt_tokens,
                        outputTokens = result.usage?.completion_tokens,
                        totalTokens = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        OpenAiChatResponseChoice(
                            finishReason = it.finish_reason,
                            index = it.index,
                            OpenAiChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<OpenAiGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): OpenAiGenerateTextRequest.Builder {
        val builder = OpenAiGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toOpenAiRequest(): OpenAiGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiAky: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<OpenAiClient>() {
        override fun build(): OpenAiClient = OpenAiClient(apiAky, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.openai.com/", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiAky")
        }
}
