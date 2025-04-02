package com.bay.aiclient.api.yandex

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class YandexHttpChatRequest(
    val modelUri: String? = null,
    val completionOptions: YandexHttpCompletionOptions,
    val messages: List<YandexHttpChatMessage>? = null,
    val jsonObject: Boolean? = null,
    val jsonSchema: JsonObject? = null,
)

@Serializable
data class YandexHttpChatMessage(
    val role: String? = null,
    val text: String? = null,
)

@Serializable
data class YandexHttpCompletionOptions(
    val stream: Boolean = false,
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val reasoningOptions: YandexHttpReasoningOptions? = null,
)

@Serializable
data class YandexHttpReasoningOptions(
    val mode: YandexHttpReasoningMode,
)

enum class YandexHttpReasoningMode {
    REASONING_MODE_UNSPECIFIED,
    DISABLED,
    ENABLED_HIDDEN,
}

@Serializable
data class YandexHttpChatResponse(
    val alternatives: List<YandexHttpChatResponseAlternative>? = null,
    val usage: YandexHttpContentUsage? = null,
    val modelVersion: String? = null,
)

@Serializable
data class YandexHttpChatResponseAlternative(
    val message: YandexHttpChatMessage? = null,
    val status: YandexHttpChatResponseAlternativeStatus? = null,
)

enum class YandexHttpChatResponseAlternativeStatus {
    ALTERNATIVE_STATUS_UNSPECIFIED,
    ALTERNATIVE_STATUS_PARTIAL,
    ALTERNATIVE_STATUS_TRUNCATED_FINAL,
    ALTERNATIVE_STATUS_FINAL,
    ALTERNATIVE_STATUS_CONTENT_FILTER,
    ALTERNATIVE_STATUS_TOOL_CALLS,
}

@Serializable
data class YandexHttpContentUsage(
    val inputTextTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null,
    val completionTokensDetails: YandexHttpCompletionTokensDetails? = null,
)

@Serializable
data class YandexHttpCompletionTokensDetails(
    val reasoningTokens: Int? = null,
)
