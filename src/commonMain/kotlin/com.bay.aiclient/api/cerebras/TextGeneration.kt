package com.bay.aiclient.api.cerebras

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class CerebrasGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val chatHistory: List<TextMessage>? = emptyList(),
    override val maxOutputTokens: Int? = null,
    override val stopSequences: List<String>? = null,
    override val temperature: Double? = null,
    override val topP: Double? = null,
    val seed: Int? = null,
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
        var seed: Int? = null,
    ) : GenerateTextRequest.Builder() {
        override fun build(): CerebrasGenerateTextRequest =
            model?.let {
                CerebrasGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    chatHistory,
                    maxOutputTokens,
                    stopSequences,
                    temperature,
                    topP,
                    seed,
                )
            }
                ?: throw IllegalStateException("Model should be set!")
    }

    companion object {
        fun builder() = Builder()
    }
}

@Serializable
data class CerebrasGenerateTextResponse(
    override val response: String? = null,
    override val usage: CerebrasGenerateTextTokenUsage? = null,
    val choices: List<CerebrasChatResponseChoice>? = null,
) : GenerateTextResponse()

@Serializable
data class CerebrasGenerateTextTokenUsage(
    override val inputTokens: Int? = null,
    override val outputTokens: Int? = null,
    override val totalTokens: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class CerebrasChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class CerebrasChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: CerebrasChatMessage? = null,
)
