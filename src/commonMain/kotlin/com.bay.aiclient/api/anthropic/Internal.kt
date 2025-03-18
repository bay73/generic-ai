package com.bay.aiclient.api.anthropic

import kotlinx.serialization.Serializable

@Serializable
data class AnthropicHttpChatRequest(
    val messages: List<AnthropicHttpChatMessage>? = null,
    val model: String? = null,
    val max_tokens: Int,
    val stop_sequences: List<String>? = null,
    val stream: Boolean? = false,
    val system: String? = null,
    val temperature: Double? = null,
    val top_k: Int? = null,
    val top_p: Double? = null,
)

@Serializable
data class AnthropicHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class AnthropicHttpChatResponse(
    val type: String? = null,
    val role: String? = null,
    val content: List<AnthropicHttpChatResponseContent>? = null,
    val model: String? = null,
    val stop_reason: String? = null,
    val stop_sequences: String? = null,
    val usage: AnthropicHttpUsage? = null,
)

@Serializable
data class AnthropicHttpChatResponseContent(
    val text: String? = null,
    val type: String? = null,
)

@Serializable
data class AnthropicHttpUsage(
    val input_tokens: Int? = null,
    val cache_creation_input_tokens: Int? = null,
    val cache_read_input_tokens: Int? = null,
    val output_tokens: Int? = null,
)

@Serializable
data class AnthropicHttpModelsResponse(
    val data: List<AnthropicHttpModel>? = null,
)

@Serializable
data class AnthropicHttpModel(
    val id: String? = null,
    val display_name: String? = null,
    val created_at: String? = null,
)
