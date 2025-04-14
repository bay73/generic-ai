package com.bay.aiclient

import com.bay.aiclient.api.ai21.Ai21Client
import com.bay.aiclient.api.anthropic.AnthropicClient
import com.bay.aiclient.api.azureopenai.AzureOpenAiClient
import com.bay.aiclient.api.bedrock.BedrockClient
import com.bay.aiclient.api.cerebras.CerebrasClient
import com.bay.aiclient.api.cohere.CohereClient
import com.bay.aiclient.api.deepseek.DeepSeekClient
import com.bay.aiclient.api.google.GoogleClient
import com.bay.aiclient.api.grok.GrokClient
import com.bay.aiclient.api.inceptionlabs.InceptionLabsClient
import com.bay.aiclient.api.mistral.MistralClient
import com.bay.aiclient.api.openai.OpenAiClient
import com.bay.aiclient.api.sambanova.SambaNovaClient
import com.bay.aiclient.api.togetherai.TogetherAiClient
import com.bay.aiclient.api.yandex.YandexClient
import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.ModelsResponse
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.logging.LogLevel
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

abstract class AiClient internal constructor(
    open var defaultModel: String? = null,
    open var defaultTemperature: Double? = null,
    open var timeout: Duration = 60.seconds,
    open var httpLogLevel: LogLevel = LogLevel.NONE,
) {
    abstract suspend fun models(): Result<ModelsResponse>

    abstract suspend fun generateText(request: GenerateTextRequest): Result<GenerateTextResponse>

    abstract suspend fun generateText(configuration: GenerateTextRequest.Builder.() -> Unit): Result<GenerateTextResponse>

    abstract fun textGenerationRequestBuilder(): GenerateTextRequest.Builder

    abstract class Builder<T : AiClient>(
        open var apiKey: String = "",
        open var defaultModel: String? = null,
        open var defaultTemperature: Double? = null,
        open var timeout: Duration = 60.seconds,
        open var httpLogLevel: LogLevel = LogLevel.NONE,
        open var httpEngine: HttpClientEngine? = null,
    ) {
        abstract fun build(): T
    }

    companion object {
        fun getBuilder(clientType: Type): Builder<out AiClient> {
            val builder = getBuilder(clientType.clazz)
            return builder
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : AiClient> getBuilder(clientClass: KClass<T>): Builder<T> =
            when (clientClass) {
                Ai21Client::class -> Ai21Client.Builder() as Builder<T>
                AnthropicClient::class -> AnthropicClient.Builder() as Builder<T>
                AzureOpenAiClient::class -> AzureOpenAiClient.Builder() as Builder<T>
                BedrockClient::class -> BedrockClient.Builder() as Builder<T>
                CerebrasClient::class -> CerebrasClient.Builder() as Builder<T>
                CohereClient::class -> CohereClient.Builder() as Builder<T>
                DeepSeekClient::class -> DeepSeekClient.Builder() as Builder<T>
                GoogleClient::class -> GoogleClient.Builder() as Builder<T>
                GrokClient::class -> GrokClient.Builder() as Builder<T>
                InceptionLabsClient::class -> InceptionLabsClient.Builder() as Builder<T>
                MistralClient::class -> MistralClient.Builder() as Builder<T>
                OpenAiClient::class -> OpenAiClient.Builder() as Builder<T>
                SambaNovaClient::class -> SambaNovaClient.Builder() as Builder<T>
                TogetherAiClient::class -> TogetherAiClient.Builder() as Builder<T>
                YandexClient::class -> YandexClient.Builder() as Builder<T>
                else -> throw IllegalArgumentException("Unsupported AIClient implementation!")
            }

        fun get(
            clientType: Type,
            configBlock: Builder<out AiClient>.() -> Unit = {},
        ): AiClient = getBuilder(clientType).also { configBlock.invoke(it) }.build()

        @Suppress("UNCHECKED_CAST")
        fun <T : AiClient, B : Builder<T>> get(
            clientClass: KClass<T>,
            configBlock: B.() -> Unit = {},
        ): T = (getBuilder(clientClass) as B).also { configBlock.invoke(it) }.build()
    }

    enum class Type(
        val clazz: KClass<out AiClient>,
    ) {
        AI21(Ai21Client::class),
        ANTHROPIC(AnthropicClient::class),
        AZURE_OPENAI(AzureOpenAiClient::class),
        BEDROCK(BedrockClient::class),
        CEREBRAS(CerebrasClient::class),
        COHERE(CohereClient::class),
        DEEP_SEEK(DeepSeekClient::class),
        GOOGLE(GoogleClient::class),
        GROK(GrokClient::class),
        INCEPTION_LABS(InceptionLabsClient::class),
        MISTRAL(MistralClient::class),
        OPEN_AI(OpenAiClient::class),
        SAMBA_NOVA(SambaNovaClient::class),
        TOGETHER_AI(TogetherAiClient::class),
        YANDEX(YandexClient::class),
    }
}
