package com.bay.aiclient.api.yandex

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.GenerateTextTokenUsage
import com.bay.aiclient.domain.ResponseFormat
import com.bay.aiclient.domain.TextMessage
import kotlinx.serialization.Serializable

@Serializable
data class YandexGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val responseFormat: ResponseFormat? = null,
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
        override var responseFormat: ResponseFormat? = null,
        override var chatHistory: List<TextMessage>? = emptyList(),
        override var maxOutputTokens: Int? = null,
        override var stopSequences: List<String>? = null,
        override var temperature: Double? = null,
    ) : GenerateTextRequest.Builder() {
        override fun build(): YandexGenerateTextRequest =
            model?.let {
                YandexGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    responseFormat,
                    chatHistory,
                    maxOutputTokens,
                    stopSequences,
                    temperature,
                )
            }
                ?: throw IllegalStateException("Model should be set!")
    }

    companion object {
        fun builder() = Builder()
    }
}

@Serializable
data class YandexGenerateTextResponse(
    override val response: String? = null,
    override val usage: YandexGenerateTextTokenUsage? = null,
    val choices: List<YandexChatResponseChoice>? = null,
) : GenerateTextResponse()

@Serializable
data class YandexGenerateTextTokenUsage(
    override val inputTokens: Int? = null,
    override val outputTokens: Int? = null,
    override val totalTokens: Int? = null,
) : GenerateTextTokenUsage()

@Serializable
data class YandexChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class YandexChatResponseChoice(
    val message: YandexChatMessage? = null,
)
