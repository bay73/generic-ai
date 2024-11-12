package com.bay.aiclient.api.ai21

import com.bay.aiclient.domain.GenerateTextRequest
import kotlinx.serialization.Serializable

@Serializable
data class Ai21HttpChatRequest(
    val model: String? = null,
    val messages: List<Ai21HttpChatMessage>? = null,
    val response_format: Ai21HttpChatResponseFormat? = null,
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
data class Ai21HttpChatResponseFormat(
    val type: String?,
) {
    companion object {
        fun from(genericResponseFormat: GenerateTextRequest.ResponseFormat?): Ai21HttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null, GenerateTextRequest.ResponseFormat.Type.TEXT -> null
                GenerateTextRequest.ResponseFormat.Type.JSON_OBJECT, GenerateTextRequest.ResponseFormat.Type.JSON_SCHEMA ->
                    Ai21HttpChatResponseFormat("json_object")
            }
    }
}

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
