package com.bay.aiclient.api.openai

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiGenerateTextRequest(
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
        override fun build(): OpenAiGenerateTextRequest =
            model?.let {
                OpenAiGenerateTextRequest(
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
data class OpenAiGenerateTextResponse(
    override val response: String? = null,
    override val usage: OpenAiGenerateTextTokenUsage? = null,
    val choices: List<OpenAiChatResponseChoice>? = null,
) : GenerateTextResponse()

@Serializable
data class OpenAiGenerateTextTokenUsage(
    override val inputToken: Int? = null,
    override val outputToken: Int? = null,
    override val totalToken: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class OpenAiChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class OpenAiChatResponseChoice(
    val finishReason: String? = null,
    val index: Int? = null,
    val message: OpenAiChatMessage? = null,
)
