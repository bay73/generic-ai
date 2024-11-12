package com.bay.aiclient.api.cerebras

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class CerebrasClient(
    apiAky: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    httpLogLevel: LogLevel = LogLevel.NONE,
) : AiClient() {
    override suspend fun models(): Result<CerebrasModelsResponse> =
        client.runGet("/v1/models") { result: CerebrasHttpModelsResponse ->
            CerebrasModelsResponse(
                result.data?.map { model ->
                    CerebrasModel(
                        id = model.id ?: "",
                        name = model.id ?: "",
                        created = model.created,
                    )
                }
                    ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<CerebrasGenerateTextResponse> =
        generateText(request.toCerebrasRequest())

    suspend fun generateText(request: CerebrasGenerateTextRequest): Result<CerebrasGenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { CerebrasHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = mutableListOf<CerebrasHttpChatMessage>()
        if (request.systemInstructions?.isNotBlank() == true) {
            newMessages.add(CerebrasHttpChatMessage(role = "system", request.systemInstructions))
        }
        newMessages.add(CerebrasHttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            CerebrasHttpChatRequest(
                model = request.model,
                messages = historyMessages + newMessages,
                max_completion_tokens = request.maxOutputTokens,
                seed = request.seed,
                stop = request.stopSequences,
                temperature = request.temperature,
                top_p = request.topP,
            )
        return client.runPost("/v1/chat/completions", httpRequest) { result: CerebrasHttpChatResponse ->
            CerebrasGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    CerebrasGenerateTextTokenUsage(
                        inputToken = result.usage?.prompt_tokens,
                        outputToken = result.usage?.completion_tokens,
                        totalToken = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        CerebrasChatResponseChoice(
                            finish_reason = it.finish_reason,
                            index = it.index,
                            CerebrasChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<CerebrasGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): CerebrasGenerateTextRequest.Builder {
        val builder = CerebrasGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toCerebrasRequest(): CerebrasGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiAky: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
    ) : AiClient.Builder<CerebrasClient>() {
        override fun build(): CerebrasClient = CerebrasClient(apiAky, defaultModel, defaultTemperature, timeout, httpLogLevel)
    }

    private val client =
        AiHttpClient("https://api.cerebras.ai", timeout, httpLogLevel) {
            append(HttpHeaders.Authorization, "Bearer $apiAky")
        }
}
