package com.bay.aiclient.api.togetherai

import kotlinx.serialization.Serializable

@Serializable
data class TogetherAiHttpChatRequest(
    val messages: List<TogetherAiHttpChatMessage>? = null,
    val model: String? = null,
    val max_tokens: Int? = null,
    val stop: List<String>? = null,
    val temperature: Double? = null,
    val top_p: Double? = null,
    val top_k: Int? = null,
    val repetition_penalty: Double? = null,
    val stream: Boolean = false,
    val presence_penalty: Double? = null,
    val frequency_penalty: Double? = null,
    val seed: Int? = null,
)

@Serializable
data class TogetherAiHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class TogetherAiHttpChatResponse(
    val choices: List<TogetherAiHttpChatResponseChoice>? = null,
    val usage: TogetherAiHttpTokenUsage? = null,
)

@Serializable
data class TogetherAiHttpChatResponseChoice(
    val index: Int? = null,
    val message: TogetherAiHttpChatMessage? = null,
    val finish_reason: String? = null,
)

@Serializable
data class TogetherAiHttpTokenUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
)

@Serializable
data class TogetherAiHttpModel(
    val id: String? = null,
    val type: String? = null,
    val display_name: String? = null,
    val created: Long? = null,
)
