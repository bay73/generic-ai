package com.bay.aiclient.api.openai

import kotlinx.serialization.Serializable

@Serializable
data class OpenAiHttpChatRequest(
    val messages: List<OpenAiHttpChatMessage>? = null,
    val model: String? = null,
    val max_completion_tokens: Int? = null,
    val temperature: Double? = null,
    val top_p: Double? = null,
)

@Serializable
data class OpenAiHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class OpenAiHttpChatResponse(
    val choices: List<OpenAiHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: OpenAiHttpUsage? = null,
)

@Serializable
data class OpenAiHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: OpenAiHttpChatMessage? = null,
)

@Serializable
data class OpenAiHttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
    val completion_tokens_details: OpenAiHttpCompletionTokenDetails? = null,
)

@Serializable
data class OpenAiHttpCompletionTokenDetails(
    val reasoning_tokens: Int? = null,
)

@Serializable
data class OpenAiHttpModelsResponse(
    val data: List<OpenAiHttpModel>? = null,
)

@Serializable
data class OpenAiHttpModel(
    val id: String? = null,
    val created: Long? = null,
)
