package com.bay.aiclient.api.sambanova

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class SambaNovaClient internal constructor(
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<SambaNovaModelsResponse> =
        Result.success(
            SambaNovaModelsResponse(
                listOf(
                    SambaNovaModel("DeepSeek-R1-Distill-Llama-70B", "DeepSeek-R1-Distill-Llama-70B"),
                    SambaNovaModel("Llama-3.1-Tulu-3-405B", "Llama-3.1-Tulu-3-405B"),
                    SambaNovaModel("Meta-Llama-3.3-70B-Instruct", "Meta-Llama-3.3-70B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.2-1B-Instruct", "Meta-Llama-3.2-1B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.2-3B-Instruct", "Meta-Llama-3.2-3B-Instruct"),
                    SambaNovaModel("Llama-3.2-11B-Vision-Instruct", "Llama-3.2-11B-Vision-Instruct"),
                    SambaNovaModel("Llama-3.2-90B-Vision-Instruct", "Llama-3.2-90B-Vision-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-8B-Instruct", "Meta-Llama-3.1-8B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-70B-Instruct", "Meta-Llama-3.1-70B-Instruct"),
                    SambaNovaModel("Meta-Llama-3.1-405B-Instruct", "Meta-Llama-3.1-405B-Instruct"),
                    SambaNovaModel("Meta-Llama-Guard-3-8B", "Meta-Llama-Guard-3-8B"),
                    SambaNovaModel("Qwen2.5-72B-Instruct", "Qwen2.5-72B-Instruct"),
                    SambaNovaModel("Qwen2.5-72B-Instruct", "Qwen2.5-72B-Instruct"),
                    SambaNovaModel("QwQ-32B", "QwQ-32B"),
                    SambaNovaModel("Qwen2-Audio-7B-Instruct", "Qwen2-Audio-7B-Instruct"),
                    SambaNovaModel("Llama-3.1-Swallow-8B-Instruct-v0.3", "Llama-3.1-Swallow-8B-Instruct-v0.3"),
                    SambaNovaModel("Llama-3.1-Swallow-70B-Instruct-v0.3", "Llama-3.1-Swallow-70B-Instruct-v0.3"),
                ),
            ),
        )

    override suspend fun generateText(request: GenerateTextRequest): Result<SambaNovaGenerateTextResponse> =
        generateText(request.toSambaNovaRequest())

    suspend fun generateText(request: SambaNovaGenerateTextRequest): Result<SambaNovaGenerateTextResponse> {
        val systemMessages =
            if (request.systemInstructions?.isNotBlank() == true) {
                listOf(SambaNovaHttpChatMessage(role = "system", request.systemInstructions))
            } else {
                emptyList()
            }
        val historyMessages = request.chatHistory?.map { SambaNovaHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = listOf(SambaNovaHttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            SambaNovaHttpChatRequest(
                model = request.model,
                messages = systemMessages + historyMessages + newMessages,
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
                        inputTokens = result.usage?.prompt_tokens,
                        outputTokens = result.usage?.completion_tokens,
                        totalTokens = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        SambaNovaChatResponseChoice(
                            index = it.index,
                            SambaNovaChatMessage(role = it.message?.role, content = it.message?.content),
                            finishReason = it.finish_reason,
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
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<SambaNovaClient>() {
        override fun build(): SambaNovaClient = SambaNovaClient(apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://api.sambanova.ai/", timeout, httpLogLevel, httpEngine) {
            append(HttpHeaders.Authorization, "Bearer $apiKey")
        }
}
