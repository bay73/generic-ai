package com.bay.aiclient.api.mistral

import kotlinx.serialization.Serializable

@Serializable
data class MistralHttpChatRequest(
    val messages: List<MistralHttpChatMessage>? = null,
    val model: String? = null,
    val max_tokens: Int? = null,
    val temperature: Double? = null,
    val top_p: Double? = null,
)

@Serializable
data class MistralHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class MistralHttpChatResponse(
    val choices: List<MistralHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: MistralHttpUsage? = null,
)

@Serializable
data class MistralHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: MistralHttpChatMessage? = null,
)

@Serializable
data class MistralHttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
)

@Serializable
data class MistralHttpModelsResponse(
    val data: List<MistralHttpModel>? = null,
)

@Serializable
data class MistralHttpModel(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val deprecation: String? = null,
    val created: Long? = null,
)
