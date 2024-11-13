package com.bay.aiclient.api.anthropic

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class AnthropicGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val chatHistory: List<TextMessage>? = emptyList(),
    override val maxOutputTokens: Int? = null,
    override val stopSequences: List<String>? = null,
    override val temperature: Double? = null,
    override val topP: Double? = null,
    val topK: Int? = null,
) : GenerateTextRequest() {
    class Builder(
        override var prompt: String = "",
        override var model: String? = null,
        override var systemInstructions: String? = null,
        override var chatHistory: List<TextMessage>? = emptyList(),
        override var maxOutputTokens: Int? = null,
        override var stopSequences: List<String>? = null,
        override var temperature: Double? = null,
        override var topP: Double? = null,
        var topK: Int? = null,
    ) : GenerateTextRequest.Builder() {
        override fun build(): AnthropicGenerateTextRequest =
            model?.let {
                AnthropicGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    chatHistory,
                    maxOutputTokens,
                    stopSequences,
                    temperature,
                    topP,
                    topK,
                )
            }
                ?: throw IllegalStateException("Model should be set!")
    }

    companion object {
        fun builder() = Builder()
    }
}

@Serializable
data class AnthropicGenerateTextResponse(
    override val response: String? = null,
    override val usage: AnthropicGenerateTextTokenUsage? = null,
    val finishReason: String? = null,
) : GenerateTextResponse()

@Serializable
data class AnthropicGenerateTextTokenUsage(
    override val inputTokens: Int? = null,
    override val outputTokens: Int? = null,
    override val totalTokens: Int? = null,
) : GenerateTextTokenUsage()
