package com.bay.aiclient.api.ai21

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class Ai21GenerateTextRequest(
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
    ) : GenerateTextRequest.Builder() {
        override fun build(): Ai21GenerateTextRequest =
            model?.let { Ai21GenerateTextRequest(it, prompt, systemInstructions, chatHistory, maxOutputTokens, stopSequences, temperature) }
                ?: throw IllegalStateException("Model should be set!")
    }

    companion object {
        fun builder() = Builder()
    }
}

@Serializable
data class Ai21GenerateTextResponse(
    override val response: String? = null,
    override val usage: Ai21GenerateTextTokenUsage? = null,
    val choices: List<Ai21ChatResponseChoice>? = null,
) : GenerateTextResponse()

@Serializable
data class Ai21GenerateTextTokenUsage(
    override val inputToken: Int? = null,
    override val outputToken: Int? = null,
    override val totalToken: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class Ai21ChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class Ai21ChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: Ai21ChatMessage? = null,
)
