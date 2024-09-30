package com.bay.aiclient.api.ai21

import kotlinx.serialization.Serializable

@Serializable
data class Ai21HttpChatRequest(
    val model: String? = null,
    val messages: List<Ai21HttpChatMessage>? = null,
    val max_tokens: Int? = null,
    val temperature: Double? = null,
    val stop: List<String>? = null,
)

@Serializable
data class Ai21HttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class Ai21HttpChatResponse(
    val choices: List<Ai21HttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: Ai21HttpUsage? = null,
)

@Serializable
data class Ai21HttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: Ai21HttpChatMessage? = null,
)

@Serializable
data class Ai21HttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
)
