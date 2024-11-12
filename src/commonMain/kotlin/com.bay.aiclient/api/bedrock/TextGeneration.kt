package com.bay.aiclient.api.bedrock

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class BedrockGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val responseFormat: ResponseFormat? = ResponseFormat.TEXT,
    override val chatHistory: List<TextMessage>? = null,
    override val maxOutputTokens: Int? = null,
    override val stopSequences: List<String>? = null,
    override val temperature: Double? = null,
    override val topP: Double? = null,
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
    ) : GenerateTextRequest.Builder() {
        override fun build(): BedrockGenerateTextRequest =
            model?.let {
                BedrockGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    ResponseFormat.TEXT,
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
data class BedrockGenerateTextResponse(
    override val response: String? = null,
    override val usage: BedrockGenerateTextTokenUsage? = null,
) : GenerateTextResponse()

@Serializable
data class BedrockGenerateTextTokenUsage(
    override val inputToken: Int? = null,
    override val outputToken: Int? = null,
    override val totalToken: Int? = null,
) : GenerateTextTokenUsage()
