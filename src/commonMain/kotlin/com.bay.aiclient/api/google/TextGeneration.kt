package com.bay.aiclient.api.google

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class GoogleGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val chatHistory: List<TextMessage>? = emptyList(),
    override val maxOutputTokens: Int? = null,
    override val stopSequences: List<String>? = null,
    override val temperature: Double? = null,
    override val topP: Double? = null,
    val topK: Double? = null,
    val presencePenalty: Double? = null,
    val frequencyPenalty: Double? = null,
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
        var topK: Double? = null,
        var presencePenalty: Double? = null,
        var frequencyPenalty: Double? = null,
    ) : GenerateTextRequest.Builder() {
        override fun build(): GoogleGenerateTextRequest =
            model?.let {
                GoogleGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    chatHistory,
                    maxOutputTokens,
                    stopSequences,
                    temperature,
                    topP,
                    topK,
                    presencePenalty,
                    frequencyPenalty,
                )
            }
                ?: throw IllegalStateException("Model should be set!")
    }

    companion object {
        fun builder() = Builder()
    }
}

@Serializable
data class GoogleGenerateTextResponse(
    override val response: String? = null,
    override val usage: GoogleGenerateTextTokenUsage? = null,
    val candidates: List<GoogleChatResponseCandidate>? = null,
) : GenerateTextResponse()

@Serializable
data class GoogleGenerateTextTokenUsage(
    override val inputToken: Int? = null,
    override val outputToken: Int? = null,
    override val totalToken: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class GoogleChatResponseCandidate(
    val finishReason: String? = null,
    val index: Int? = null,
    val message: TextMessage? = null,
)
