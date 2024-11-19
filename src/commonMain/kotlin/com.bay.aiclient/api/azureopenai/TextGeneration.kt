package com.bay.aiclient.api.azureopenai

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.ResponseFormat
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class AzureOpenAiGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val responseFormat: ResponseFormat? = null,
    override val chatHistory: List<TextMessage>? = emptyList(),
    override val maxOutputTokens: Int? = null,
    override val stopSequences: List<String>? = null,
    override val temperature: Double? = null,
    override val topP: Double? = null,
    val seed: Int? = null,
    val frequencyPenalty: Double? = null,
    val presencePenalty: Double? = null,
) : GenerateTextRequest() {
    class Builder(
        override var prompt: String = "",
        override var model: String? = null,
        override var systemInstructions: String? = null,
        override var responseFormat: ResponseFormat? = null,
        override var chatHistory: List<TextMessage>? = emptyList(),
        override var maxOutputTokens: Int? = null,
        override var stopSequences: List<String>? = null,
        override var temperature: Double? = null,
        override var topP: Double? = null,
        var seed: Int? = null,
        var frequencyPenalty: Double? = null,
        var presencePenalty: Double? = null,
    ) : GenerateTextRequest.Builder() {
        override fun build(): AzureOpenAiGenerateTextRequest =
            model?.let {
                AzureOpenAiGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    responseFormat,
                    chatHistory,
                    maxOutputTokens,
                    stopSequences,
                    temperature,
                    topP,
                    seed,
                    frequencyPenalty,
                    presencePenalty,
                )
            }
                ?: throw IllegalStateException("Model should be set!")
    }

    companion object {
        fun builder() = Builder()
    }
}

@Serializable
data class AzureOpenAiGenerateTextResponse(
    override val response: String? = null,
    override val usage: AzureOpenAiGenerateTextTokenUsage? = null,
    val choices: List<AzureOpenAiChatResponseChoice>? = null,
) : GenerateTextResponse()

@Serializable
data class AzureOpenAiGenerateTextTokenUsage(
    override val inputTokens: Int? = null,
    override val outputTokens: Int? = null,
    override val totalTokens: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class AzureOpenAiChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class AzureOpenAiChatResponseChoice(
    val finishReason: String? = null,
    val index: Int? = null,
    val message: AzureOpenAiChatMessage? = null,
)
