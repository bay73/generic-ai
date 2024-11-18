package com.bay.aiclient.api.togetherai

import com.bay.aiclient.domain.ResponseFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

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
    val response_format: TogetherAiHttpChatResponseFormat? = null,
)

@Serializable
data class TogetherAiHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class TogetherAiHttpChatResponseFormat(
    val type: String?,
    val schema: JsonObject? = null,
) {
    companion object {
        fun from(genericResponseFormat: ResponseFormat?): TogetherAiHttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null, ResponseFormat.Type.TEXT -> null
                ResponseFormat.Type.JSON_OBJECT -> TogetherAiHttpChatResponseFormat("json_object")
                ResponseFormat.Type.JSON_SCHEMA ->
                    TogetherAiHttpChatResponseFormat(
                        "json_object",
                        genericResponseFormat.schema,
                    )
            }
    }
}

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
