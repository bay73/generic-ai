package com.bay.aiclient.api.ai21

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.http.HttpHeaders
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Ai21Client(
    apiAky: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    httpLogLevel: LogLevel = LogLevel.NONE,
) : AiClient() {
    override suspend fun models(): Result<Ai21ModelsResponse> =
        Result.success(
            Ai21ModelsResponse(listOf(Ai21Model("jamba-1.5-large", "jamba-1.5-large"), Ai21Model("jamba-1.5-mini", "jamba-1.5-mini"))),
        )

    override suspend fun generateText(request: GenerateTextRequest): Result<Ai21GenerateTextResponse> =
        generateText(request.toAi21Request())

    suspend fun generateText(request: Ai21GenerateTextRequest): Result<Ai21GenerateTextResponse> {
        val historyMessages = request.chatHistory?.map { Ai21HttpChatMessage(it.role, it.content) } ?: emptyList()
        val newMessages = mutableListOf<Ai21HttpChatMessage>()
        if (request.systemInstructions?.isNotBlank() == true) {
            newMessages.add(Ai21HttpChatMessage(role = "system", request.systemInstructions))
        }
        newMessages.add(Ai21HttpChatMessage(role = "user", request.prompt))
        val httpRequest =
            Ai21HttpChatRequest(
                model = request.model,
                messages = historyMessages + newMessages,
                max_tokens = request.maxOutputTokens,
                temperature = request.temperature,
                stop = request.stopSequences,
            )
        return client.runPost("/studio/v1/chat/completions", httpRequest) { result: Ai21HttpChatResponse ->
            Ai21GenerateTextResponse(
                response =
                    result.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content ?: "",
                usage =
                    Ai21GenerateTextTokenUsage(
                        inputToken = result.usage?.prompt_tokens,
                        outputToken = result.usage?.completion_tokens,
                        totalToken = result.usage?.total_tokens,
                    ),
                choices =
                    result.choices?.map {
                        Ai21ChatResponseChoice(
                            finish_reason = it.finish_reason,
                            index = it.index,
                            Ai21ChatMessage(role = it.message?.role, content = it.message?.content),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<Ai21GenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): Ai21GenerateTextRequest.Builder {
        val builder = Ai21GenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toAi21Request(): Ai21GenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiAky: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
    ) : AiClient.Builder<Ai21Client>() {
        override fun build(): Ai21Client = Ai21Client(apiAky, defaultModel, defaultTemperature, timeout, httpLogLevel)
    }

    private val client =
        AiHttpClient("https://api.ai21.com", timeout, httpLogLevel) {
            append(HttpHeaders.Authorization, "Bearer $apiAky")
        }
}
