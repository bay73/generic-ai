package com.bay.aiclient.api.sambanova

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class SambaNovaGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val responseFormat: ResponseFormat? = null,
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
        override var responseFormat: ResponseFormat? = null,
        override var chatHistory: List<TextMessage>? = emptyList(),
        override var maxOutputTokens: Int? = null,
        override var stopSequences: List<String>? = null,
        override var temperature: Double? = null,
        override var topP: Double? = null,
        var topK: Int? = null,
    ) : GenerateTextRequest.Builder() {
        override fun build(): SambaNovaGenerateTextRequest =
            model?.let {
                SambaNovaGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    responseFormat,
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
data class SambaNovaGenerateTextResponse(
    override val response: String? = null,
    override val usage: SambaNovaGenerateTextTokenUsage? = null,
    val choices: List<SambaNovaChatResponseChoice>? = null,
) : GenerateTextResponse()

@Serializable
data class SambaNovaGenerateTextTokenUsage(
    override val inputToken: Int? = null,
    override val outputToken: Int? = null,
    override val totalToken: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class SambaNovaChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class SambaNovaChatResponseChoice(
    val index: Int? = null,
    val message: SambaNovaChatMessage? = null,
)
