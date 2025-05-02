package com.bay.aiclient.api.novita

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.String
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class NovitaClient internal constructor(
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<NovitaModelsResponse> =
        client.runGet("/v3/openai/models") { result: NovitaHttpModelsResponse ->
            NovitaModelsResponse(
                result.data?.map { model -> NovitaModel(id = model.id ?: "", name = model.display_name ?: "", description = model.description, created = model.created, contextSize = model.context_size) }
                    ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<NovitaGenerateTextResponse> =
        generateText(request.toNovitaRequest())

    suspend fun generateText(request: NovitaGenerateTextRequest): Result<NovitaGenerateTextResponse> {
        val systemMessages =
            if (request.systemInstructions?.isNotBlank() == true) {
                listOf(NovitaHttpChatMessage(role = "system", request.systemInstructions))
            } else {
                emptyList()
            }
        val historyMessages = request.chatHistory?.map { NovitaHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = listOf(NovitaHttpChatMessage(role = "user", request.prompt))

        val httpRequest =
            NovitaHttpChatRequest(
                model = request.model,
                messages = systemMessages + historyMessages + newMessages,
                max_tokens = request.maxOutputTokens,
                stream = false,
                seed = request.seed,
                frequency_penalty = request.frequencyPenalty,
                presence_penalty = request.presencePenalty,
                repetition_penalty = request.repetitionPenalty,
                stop = request.stopSequences,
                temperature = request.temperature,
                top_p = request.topP,
                top_k = request.topK,
                min_p = request.minP,
                response_format = NovitaHttpChatResponseFormat.from(request.responseFormat),
            )
        return client.runPost("/v3/openai/chat/completions", httpRequest) { result: NovitaHttpChatResponse ->
            NovitaGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    NovitaGenerateTextTokenUsage(
                        inputTokens = result.usage?.prompt_tokens,
                        outputTokens = result.usage?.completion_tokens,
                        totalTokens = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        NovitaChatResponseChoice(
                            finishReason = it.finish_reason,
                            index = it.index,
                            NovitaChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<NovitaGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): NovitaGenerateTextRequest.Builder {
        val builder = NovitaGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toNovitaRequest(): NovitaGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<NovitaClient>() {
        override fun build(): NovitaClient = NovitaClient(apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.novita.ai", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiKey")
        }
}
