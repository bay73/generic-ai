package com.bay.aiclient.api.inceptionlabs

import kotlinx.serialization.Serializable

@Serializable
data class InceptionLabsHttpChatRequest(
    val messages: List<InceptionLabsHttpChatMessage>? = null,
    val model: String? = null,
    val temperature: Double? = null,
    val max_tokens: Int? = null,
    val top_p: Double? = null,
    val frequency_penalty: Double? = null,
    val presence_penalty: Double? = null,
    val stop: List<String>? = null,
    val stream: Boolean? = false,
    val diffusing: Boolean? = false,
)

@Serializable
data class InceptionLabsHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class InceptionLabsHttpChatResponse(
    val choices: List<InceptionLabsHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: InceptionLabsHttpUsage? = null,
)

@Serializable
data class InceptionLabsHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: InceptionLabsHttpChatMessage? = null,
)

@Serializable
data class InceptionLabsHttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
)
