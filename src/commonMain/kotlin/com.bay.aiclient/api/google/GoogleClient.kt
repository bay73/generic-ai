package com.bay.aiclient.api.google

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.TextMessage
import com.bay.aiclient.utils.AiHttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class GoogleClient internal constructor(
    apiAky: String,
    override var defaultModel: String? = null,
    override var defaultTemperature: Double? = null,
    override var timeout: Duration = 60.seconds,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
) : AiClient() {
    override suspend fun models(): Result<GoogleModelsResponse> =
        client.runGet("/v1beta/models") { result: GoogleHttpModelsResponse ->
            GoogleModelsResponse(
                result.models?.map { model -> GoogleModel(id = model.name ?: "", name = model.displayName ?: "") }
                    ?: emptyList(),
            )
        }

    override suspend fun generateText(request: GenerateTextRequest): Result<GoogleGenerateTextResponse> =
        generateText(request.toGoogleRequest())

    suspend fun generateText(request: GoogleGenerateTextRequest): Result<GoogleGenerateTextResponse> {
        val model = request.model
        val historyContent =
            request.chatHistory?.map { GoogleHttpChatContent(it.role, listOf(GoogleMessageTextPart(it.content))) } ?: emptyList()
        val newContent = listOf(GoogleHttpChatContent(role = "user", listOf(GoogleMessageTextPart(request.prompt))))
        val httpRequest =
            GoogleHttpChatRequest(
                contents = historyContent + newContent,
                systemInstruction = request.systemInstructions?.let { GoogleHttpChatContent(parts = listOf(GoogleMessageTextPart(it))) },
                generationConfig =
                    GoogleHttpGenerationConfig(
                        stopSequences = request.stopSequences,
                        responseMimeType = GoogleHttpGenerationConfig.mimeTypeFrom(request.responseFormat),
                        responseSchema = request.responseFormat?.schema,
                        maxOutputTokens = request.maxOutputTokens,
                        temperature = request.temperature,
                        topP = request.topP,
                        topK = request.topK,
                        presencePenalty = request.presencePenalty,
                        frequencyPenalty = request.frequencyPenalty,
                        candidateCount = request.candidateCount,
                    ),
            )
        return client.runPost("/v1beta/$model:generateContent", httpRequest) { result: GoogleHttpChatResponse ->
            GoogleGenerateTextResponse(
                response =
                    result.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.first()
                        ?.text ?: "",
                usage =
                    GoogleGenerateTextTokenUsage(
                        inputTokens = result.usageMetadata?.promptTokenCount,
                        outputTokens = result.usageMetadata?.candidatesTokenCount,
                        totalTokens = result.usageMetadata?.totalTokenCount,
                    ),
                candidates =
                    result.candidates?.map {
                        GoogleChatResponseCandidate(
                            finishReason = it.finishReason,
                            index = it.index,
                            TextMessage(
                                role = it.content?.role,
                                content =
                                    it.content
                                        ?.parts
                                        ?.first()
                                        ?.text ?: "",
                            ),
                        )
                    },
            )
        }
    }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<GoogleGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): GoogleGenerateTextRequest.Builder {
        val builder = GoogleGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toGoogleRequest(): GoogleGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        override var apiAky: String = "",
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
        override var httpLogLevel: LogLevel = LogLevel.NONE,
        override var httpEngine: HttpClientEngine? = null,
    ) : AiClient.Builder<GoogleClient>() {
        override fun build(): GoogleClient = GoogleClient(apiAky, defaultModel, defaultTemperature, timeout, httpLogLevel, httpEngine)
    }

    private val client =
        AiHttpClient("https://generativelanguage.googleapis.com/", timeout, httpLogLevel, httpEngine) {
            append("x-goog-api-key", apiAky)
        }
}
