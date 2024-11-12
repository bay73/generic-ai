package com.bay.aiclient.api.cerebras

import kotlinx.serialization.Serializable

@Serializable
data class CerebrasHttpChatRequest(
    val messages: List<CerebrasHttpChatMessage>? = null,
    val model: String? = null,
    val max_completion_tokens: Int? = null,
    val seed: Int? = null,
    val stop: List<String>? = null,
    val temperature: Double? = null,
    val top_p: Double? = null,
)

@Serializable
data class CerebrasHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class CerebrasHttpChatResponse(
    val choices: List<CerebrasHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: CerebrasHttpUsage? = null,
)

@Serializable
data class CerebrasHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: CerebrasHttpChatMessage? = null,
)

@Serializable
data class CerebrasHttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
)

@Serializable
data class CerebrasHttpModelsResponse(
    val data: List<CerebrasHttpModel>? = null,
)

@Serializable
data class CerebrasHttpModel(
    val id: String? = null,
    val created: Long? = null,
)
