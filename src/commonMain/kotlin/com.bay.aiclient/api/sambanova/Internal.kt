package com.bay.aiclient.api.sambanova

import kotlinx.serialization.Serializable

@Serializable
data class SambaNovaHttpChatRequest(
    val messages: List<SambaNovaHttpChatMessage>? = null,
    val model: String? = null,
    val max_tokens: Int? = null,
    val temperature: Double? = null,
    val top_p: Double? = null,
    val top_k: Int? = null,
    val stop: List<String>? = null,
    val stream: Boolean? = false,
)

@Serializable
data class SambaNovaHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class SambaNovaHttpChatResponse(
    val choices: List<SambaNovaHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: SambaNovaHttpUsage? = null,
)

@Serializable
data class SambaNovaHttpChatResponseChoice(
    val index: Int? = null,
    val message: SambaNovaHttpChatMessage? = null,
    val finish_reason: String? = null,
)

@Serializable
data class SambaNovaHttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
)
