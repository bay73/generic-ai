package com.bay.aiclient.api.cohere

import kotlinx.serialization.Serializable

@Serializable
data class CohereHttpChatRequest(
    val message: String,
    val stream: Boolean = false,
    val model: String? = null,
    val preamble: String? = null,
    val chat_history: List<CohereHttpChatMessage>? = null,
    val temperature: Double? = null,
    val max_tokens: Int? = null,
    val max_input_tokens: Int? = null,
    val k: Double? = null,
    val p: Double? = null,
    val seed: Int? = null,
    val stop_sequences: List<String>? = null,
    val frequency_penalty: Double? = null,
    val presence_penalty: Double? = null,
)

@Serializable
data class CohereHttpChatMessage(
    val role: String? = null,
    val message: String? = null,
)

@Serializable
data class CohereHttpChatResponse(
    val text: String? = null,
    val finish_reason: String? = null,
    val meta: CohereHttpChatResponseMeta? = null,
)

@Serializable
data class CohereHttpChatResponseMeta(
    val tokens: CohereHttpTokenUsage? = null,
)

@Serializable
data class CohereHttpTokenUsage(
    val input_tokens: Int? = null,
    val output_tokens: Int? = null,
)

@Serializable
data class CohereHttpModelsResponse(
    val models: List<CohereHttpModel>? = null,
)

@Serializable
data class CohereHttpModel(
    val name: String? = null,
)
