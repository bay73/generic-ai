package com.bay.aiclient.api.azureopenai

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class AzureOpenAiClient internal constructor(
    resourceName: String,
    apiKey: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<AzureOpenAiModelsResponse> =
        Result.success(
            AzureOpenAiModelsResponse(
                listOf(
                    AzureOpenAiModel("gpt-4o", "gpt-4o"),
                    AzureOpenAiModel("gpt-4o-mini", "gpt-4o-mini"),
                    AzureOpenAiModel("gpt-4", "gpt-4"),
                    AzureOpenAiModel("gpt-4-32k", "gpt-4-32k"),
                    AzureOpenAiModel("gpt-4-32k", "gpt-4-32k"),
                    AzureOpenAiModel("gpt-4-32k", "gpt-4-32k"),
                    AzureOpenAiModel("gpt-35-turbo", "gpt-35-turbo"),
                    AzureOpenAiModel("babbage-002", "babbage-002"),
                ),
            ),
        )

    override suspend fun generateText(request: GenerateTextRequest): Result<AzureOpenAiGenerateTextResponse> =
        generateText(request.toAzureOpenAiRequest())

    suspend fun generateText(request: AzureOpenAiGenerateTextRequest): Result<AzureOpenAiGenerateTextResponse> {
        val systemMessages =
            if (request.systemInstructions?.isNotBlank() == true) {
                listOf(AzureOpenAiHttpChatMessage(role = "system", request.systemInstructions))
            } else {
                emptyList()
            }
        val historyMessages = request.chatHistory?.map { AzureOpenAiHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = listOf(AzureOpenAiHttpChatMessage(role = "user", request.prompt))

        val httpRequest =
            AzureOpenAiHttpChatRequest(
                temperature = request.temperature,
                top_p = request.topP,
                stream = false,
                stop = request.stopSequences,
                max_tokens = request.maxOutputTokens,
                presence_penalty = request.presencePenalty,
                frequency_penalty = request.frequencyPenalty,
                messages = systemMessages + historyMessages + newMessages,
                response_format = AzureOpenAiHttpChatResponseFormat.from(request.responseFormat),
                seed = request.seed,
            )
        return client.runPost(
            "/openai/deployments/${request.model}/chat/completions?api-version=$API_VERSION",
            httpRequest,
        ) { result: AzureOpenAiHttpChatResponse ->
            AzureOpenAiGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    AzureOpenAiGenerateTextTokenUsage(
                        inputTokens = result.usage?.prompt_tokens,
                        outputTokens = result.usage?.completion_tokens,
                        totalTokens = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        AzureOpenAiChatResponseChoice(
                            finishReason = it.finish_reason,
                            index = it.index,
                            AzureOpenAiChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<AzureOpenAiGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): AzureOpenAiGenerateTextRequest.Builder {
        val builder = AzureOpenAiGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toAzureOpenAiRequest(): AzureOpenAiGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        var resourceName: String = "",
        override var apiKey: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<AzureOpenAiClient>() {
        override fun build(): AzureOpenAiClient {
            if (resourceName.isEmpty()) {
                throw IllegalStateException("Azure resource name should be specified to initialize AzureOpenAiClient.")
            }
            return AzureOpenAiClient(resourceName, apiKey, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
        }
    }

    private val client =
        AiHttpClient("https://$resourceName.openai.azure.com", timeout, httpLogLevel, httpEngine) {
            append("api-key", apiKey)
        }

    companion object {
        private const val API_VERSION = "2024-10-21"
    }
}
