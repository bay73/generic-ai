package com.bay.aiclient.api.bedrock

import com.bay.aiclient.AiClient
import com.bay.aiclient.domain.GenerateTextRequest
import io.ktor.client.plugins.logging.LogLevel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BedrockClient internal constructor(
    val credentials: Credentials,
    override var defaultModel: String? = null,
    override var timeout: Duration = 60.seconds,
    override var defaultTemperature: Double? = null,
    override var httpLogLevel: LogLevel = LogLevel.NONE,
) : AiClient() {
    override suspend fun models(): Result<BedrockModelsResponse> = kotlin.runCatching { internalClient.models() }

    override suspend fun generateText(request: GenerateTextRequest): Result<BedrockGenerateTextResponse> =
        generateText(request.toBedrockRequest())

    suspend fun generateText(request: BedrockGenerateTextRequest): Result<BedrockGenerateTextResponse> =
        kotlin.runCatching { internalClient.generateText(request) }

    override suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<BedrockGenerateTextResponse> {
        val builder = textGenerationRequestBuilder()
        configuration.invoke(builder)
        return generateText(builder.build())
    }

    override fun textGenerationRequestBuilder(): BedrockGenerateTextRequest.Builder {
        val builder = BedrockGenerateTextRequest.Builder(temperature = defaultTemperature)
        defaultModel?.also { builder.model = it }
        return builder
    }

    private fun GenerateTextRequest.toBedrockRequest(): BedrockGenerateTextRequest =
        textGenerationRequestBuilder().also { builder -> this.copyTo(builder) }.build()

    class Builder(
        var credentials: Credentials = Credentials(),
        override var defaultModel: String? = null,
        override var defaultTemperature: Double? = null,
        override var timeout: Duration = 60.seconds,
    ) : AiClient.Builder<BedrockClient>() {
        override fun build(): BedrockClient =
            if (apiAky.isBlank()) {
                BedrockClient(credentials, defaultModel, timeout, defaultTemperature)
            } else {
                throw IllegalStateException("API key is not supported by BedrockClient. Use BedrockClient.Credentials")
            }
    }

    private val internalClient: BedrockClientInternal = BedrockClientInternal(credentials, timeout)

    data class Credentials(
        val region: String = "eu-central-1",
        val useDefault: Boolean = true,
        val accessKeyId: String = "",
        val secretAccessKey: String = "",
        val sessionToken: String = "",
    )
}

expect class BedrockClientInternal(
    credentials: BedrockClient.Credentials,
    timeout: Duration,
) {
    suspend fun models(): BedrockModelsResponse

    suspend fun generateText(request: BedrockGenerateTextRequest): BedrockGenerateTextResponse
}
