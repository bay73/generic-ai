package com.bay.aiclient.api.togetherai

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class TogetherAiGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val chatHistory: List<TextMessage>? = emptyList(),
    override val maxOutputTokens: Int? = null,
    override val stopSequences: List<String>? = null,
    override val temperature: Double? = null,
    override val topP: Double? = null,
    val topK: Int? = null,
    val seed: Int? = null,
    val repetitionPenalty: Double? = null,
    val frequencyPenalty: Double? = null,
    val presencePenalty: Double? = null,
) : GenerateTextRequest() {
    class Builder(
        override var prompt: String = "",
        override var model: String? = null,
        override var systemInstructions: String? = null,
        override var chatHistory: List<TextMessage>? = null,
        override var maxOutputTokens: Int? = null,
        override var stopSequences: List<String>? = null,
        override var temperature: Double? = null,
        override var topP: Double? = null,
        var topK: Int? = null,
        var seed: Int? = null,
        var repetitionPenalty: Double? = null,
        var frequencyPenalty: Double? = null,
        var presencePenalty: Double? = null,
    ) : GenerateTextRequest.Builder() {
        override fun build(): TogetherAiGenerateTextRequest =
            model?.let {
                TogetherAiGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    chatHistory,
                    maxOutputTokens,
                    stopSequences,
                    temperature,
                    topP,
                    topK,
                    seed,
                    repetitionPenalty,
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
data class TogetherAiGenerateTextResponse(
    override val response: String? = null,
    override val usage: TogetherAiGenerateTextTokenUsage? = null,
) : GenerateTextResponse()

@Serializable
data class TogetherAiGenerateTextTokenUsage(
    override val inputToken: Int? = null,
    override val outputToken: Int? = null,
    override val totalToken: Int? = null,
) : GenerateTextTokenUsage()
