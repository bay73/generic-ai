package com.bay.aiclient.api.grok

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.ResponseFormat
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class GrokGenerateTextRequest(
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
        override var chatHistory: List<TextMessage>? = null,
        override var maxOutputTokens: Int? = null,
        override var stopSequences: List<String>? = null,
        override var temperature: Double? = null,
        override var topP: Double? = null,
        var seed: Int? = null,
        var frequencyPenalty: Double? = null,
        var presencePenalty: Double? = null,
    ) : GenerateTextRequest.Builder() {
        override fun build(): GrokGenerateTextRequest =
            model?.let {
                GrokGenerateTextRequest(
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
data class GrokGenerateTextResponse(
    override val response: String? = null,
    override val usage: GrokGenerateTextTokenUsage? = null,
    val choices: List<GrokChatResponseChoice>? = null,
) : GenerateTextResponse()

@Serializable
data class GrokGenerateTextTokenUsage(
    override val inputTokens: Int? = null,
    override val outputTokens: Int? = null,
    override val totalTokens: Int? = null,
    val reasoningTokens: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class GrokChatResponseChoice(
    val finishReason: String? = null,
    val index: Int? = null,
    val message: GrokChatMessage? = null,
)

@Serializable
data class GrokChatMessage(
    val role: String? = null,
    val content: String? = null,
)
