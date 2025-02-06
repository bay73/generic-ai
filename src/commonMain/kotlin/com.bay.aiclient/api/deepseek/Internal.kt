package com.bay.aiclient.api.deepseek

import com.bay.aiclient.domain.ResponseFormat
import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekHttpChatRequest(
    val messages: List<DeepSeekHttpChatMessage>? = null,
    val model: String? = null,
    val frequency_penalty: Double? = null,
    val max_tokens: Int? = null,
    val presence_penalty: Double? = null,
    val response_format: DeepSeekHttpChatResponseFormat? = null,
    val stop: List<String>? = null,
    val stream: Boolean? = false,
    val temperature: Double? = null,
    val top_p: Double? = null,
)

@Serializable
data class DeepSeekHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class DeepSeekHttpChatResponseFormat(
    val type: String?,
) {
    companion object {
        fun from(genericResponseFormat: ResponseFormat?): DeepSeekHttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null -> null
                ResponseFormat.Type.TEXT -> DeepSeekHttpChatResponseFormat("text")
                ResponseFormat.Type.JSON_OBJECT, ResponseFormat.Type.JSON_SCHEMA -> DeepSeekHttpChatResponseFormat("json_object")
            }
    }
}

@Serializable
data class DeepSeekHttpChatResponse(
    val choices: List<DeepSeekHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: DeepSeekHttpUsage? = null,
)

@Serializable
data class DeepSeekHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: DeepSeekHttpChatMessage? = null,
)

@Serializable
data class DeepSeekHttpUsage(
    val completion_tokens: Int? = null,
    val prompt_tokens: Int? = null,
    val total_tokens: Int? = null,
    val completion_tokens_details: DeepSeekHttpCompletionTokenDetails? = null,
)

@Serializable
data class DeepSeekHttpCompletionTokenDetails(
    val reasoning_tokens: Int? = null,
)

@Serializable
data class DeepSeekHttpModelsResponse(
    val data: List<DeepSeekHttpModel>? = null,
)

@Serializable
data class DeepSeekHttpModel(
    val id: String? = null,
    val owned_by: String? = null,
)
