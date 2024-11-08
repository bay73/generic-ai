package com.bay.aiclient.api.sambanova

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SambaNovaClient(
    apiAky: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    httpLogLevel: LogLevel = LogLevel.NONE,
) : AiClient() {
    override suspend fun models(): Result<SambaNovaModelsResponse> =
        Result.success(
            SambaNovaModelsResponse(
                listOf(
                    SambaNovaModel("Meta-Llama-3.2-1B-Instruct", "Meta-Llama-3.2-1B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.2-3B-Instruct", "Meta-Llama-3.2-3B-Instruct"),
                    SambaNovaModel("Llama-3.2-11B-Vision-Instruct", "Llama-3.2-11B-Vision-Instruct"),
                    SambaNovaModel("Llama-3.2-90B-Vision-Instruct", "Llama-3.2-90B-Vision-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-8B-Instruct", "Meta-Llama-3.1-8B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-70B-Instruct", "Meta-Llama-3.1-70B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-405B-Instruct", "Meta-Llama-3.1-405B-Instruct"),
                ),
            ),
        )

    override suspend fun generateText(request: GenerateTextRequest): Result<SambaNovaGenerateTextResponse> =
        generateText(request.toSambaNovaRequest())

    suspend fun generateText(request: SambaNovaGenerateTextRequest): Result<SambaNovaGenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { SambaNovaHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = mutableListOf<SambaNovaHttpChatMessage>()
        if (request.systemInstructions?.isNotBlank() == true) {
            newMessages.add(SambaNovaHttpChatMessage(role = "system", request.systemInstructions))
        }
        newMessages.add(SambaNovaHttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            SambaNovaHttpChatRequest(
                model = request.model,
                messages = historyMessages + newMessages,
                max_tokens = request.maxOutputTokens,
                temperature = request.temperature,
                top_p = request.topP,
                top_k = request.topK,
                stop = request.stopSequences,
            )
        return client.runPost("/v1/chat/completions", httpRequest) { result: SambaNovaHttpChatResponse ->
            SambaNovaGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    SambaNovaGenerateTextTokenUsage(
                        inputToken = result.usage?.input_tokens_count,
                        outputToken = result.usage?.output_tokens_count,
                        totalToken = result.usage?.total_tokens_count,
                    ),
                choices =
                    result.choices?.map {
                        SambaNovaChatResponseChoice(
                            finish_reason = it.finish_reason,
                            index = it.index,
                            SambaNovaChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<SambaNovaGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): SambaNovaGenerateTextRequest.Builder {
        val builder = SambaNovaGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toSambaNovaRequest(): SambaNovaGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiAky: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
    ) : AiClient.Builder<SambaNovaClient>() {
        override fun build(): SambaNovaClient = SambaNovaClient(apiAky, defaultModel, defaultTemperature, timeout, httpLogLevel)
    }

    private val client =
        AiHttpClient("https://api.sambanova.ai/", timeout, httpLogLevel) {
            append(HttpHeaders.Authorization, "Bearer $apiAky")
        }
}
