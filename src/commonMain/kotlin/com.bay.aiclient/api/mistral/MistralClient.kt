package com.bay.aiclient.api.mistral

import com.bay.aiclient.AiClient
import com.bay.aiclient.api.ai21.Ai21HttpChatMessage
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders

class MistralClient(
    apiAky: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    httpLogLevel: LogLevel = LogLevel.NONE,
) : AiClient() {
    override suspend fun models(): Result<MistralModelsResponse> =
        client.runGet("/v1/models") { result: MistralHttpModelsResponse ->
            MistralModelsResponse(
                result.data?.map { model ->
                    MistralModel(
                        id = model.id ?: "",
                        name = model.name ?: "",
                        description = model.description,
                        deprecation = model.deprecation,
                        created = model.created,
                    )
                }
                    ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<MistralGenerateTextResponse> =
        generateText(request.toMistralRequest())

    suspend fun generateText(request: MistralGenerateTextRequest): Result<MistralGenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { MistralHttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = mutableListOf<MistralHttpChatMessage>();
        if (request.systemInstructions?.isNotBlank()==true) {
            newMessages.add(MistralHttpChatMessage(role = "system", request.systemInstructions))
        }
        newMessages.add(MistralHttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            MistralHttpChatRequest(
                model = request.model,
                messages = historyMessages + newMessages,
                max_tokens = request.maxOutputTokens,
                temperature = request.temperature,
                top_p = request.topP,
            )
        return client.runPost("/v1/chat/completions", httpRequest) { result: MistralHttpChatResponse ->
            MistralGenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    MistralGenerateTextTokenUsage(
                        inputToken = result.usage?.prompt_tokens,
                        outputToken = result.usage?.completion_tokens,
                        totalToken = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        MistralChatResponseChoice(
                            finish_reason = it.finish_reason,
                            index = it.index,
                            MistralChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<MistralGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): MistralGenerateTextRequest.Builder {
        val builder = MistralGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toMistralRequest(): MistralGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiAky: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
    ) : AiClient.Builder<MistralClient>() {
        override fun build(): MistralClient = MistralClient(apiAky, defaultModel, defaultTemperature, httpLogLevel)
    }

    private val client =
        AiHttpClient("https://api.mistral.ai", httpLogLevel) {
            append(HttpHeaders.Authorization, "Bearer $apiAky")
        }
}
