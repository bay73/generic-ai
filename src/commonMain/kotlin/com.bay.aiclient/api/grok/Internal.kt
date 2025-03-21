package com.bay.aiclient.api.grok

import com.bay.aiclient.domain.ResponseFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GrokHttpChatRequest(
    val messages: List<GrokHttpChatMessage>? = null,
    val model: String? = null,
    val deferred: Boolean = false,
    val frequency_penalty: Double? = null,
    val max_tokens: Int? = null,
    val n: Int? = null,
    val presence_penalty: Double? = null,
    val response_format: GrokHttpChatResponseFormat? = null,
    val seed: Int? = null,
    val stop: List<String>? = null,
    val stream: Boolean = false,
    val temperature: Double? = null,
    val top_p: Double? = null,
)

@Serializable
data class GrokHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class GrokHttpChatResponseFormat(
    val type: String?,
    val json_schema: JsonObject? = null,
) {
    companion object {
        fun from(genericResponseFormat: ResponseFormat?): GrokHttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null -> null
                ResponseFormat.Type.TEXT -> GrokHttpChatResponseFormat("text")
                ResponseFormat.Type.JSON_OBJECT -> GrokHttpChatResponseFormat("json_object")
                ResponseFormat.Type.JSON_SCHEMA ->
                    GrokHttpChatResponseFormat(
                        "json_schema",
                        genericResponseFormat.schema,
                    )
            }
    }
}

@Serializable
data class GrokHttpChatResponse(
    val choices: List<GrokHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: GrokHttpTokenUsage? = null,
)

@Serializable
data class GrokHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: GrokHttpChatMessage? = null,
)

@Serializable
data class GrokHttpTokenUsage(
    val completion_tokens: Int? = null,
    val prompt_tokens: Int? = null,
    val reasoning_tokens: Int? = null,
    val total_tokens: Int? = null,
)

@Serializable
data class GrokHttpModelsResponse(
    val data: List<GrokHttpModel>? = null,
)

@Serializable
data class GrokHttpModel(
    val id: String? = null,
    val created: Long? = null,
)
