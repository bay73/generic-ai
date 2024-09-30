package com.bay.aiclient.api.mistral

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class MistralGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val chatHistory: List<TextMessage>? = emptyList(),
    override val maxOutputTokens: Int? = null,
    override val stopSequences: List<String>? = null,
    override val temperature: Double? = null,
    override val topP: Double? = null,
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
    ) : GenerateTextRequest.Builder() {
        override fun build(): MistralGenerateTextRequest =
            model?.let {
                MistralGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    chatHistory,
                    maxOutputTokens,
                    stopSequences,
                    temperature,
                    topP,
                )
            }
                ?: throw IllegalStateException("Model should be set!")
    }

    companion object {
        fun builder() = Builder()
    }
}

@Serializable
data class MistralGenerateTextResponse(
    override val response: String? = null,
    override val usage: MistralGenerateTextTokenUsage? = null,
    val choices: List<MistralChatResponseChoice>? = null,
) : GenerateTextResponse()

@Serializable
data class MistralGenerateTextTokenUsage(
    override val inputToken: Int? = null,
    override val outputToken: Int? = null,
    override val totalToken: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class MistralChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class MistralChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: MistralChatMessage? = null,
)
