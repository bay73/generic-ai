package com.bay.aiclient.api.novita

import com.bay.aiclient.domain.ResponseFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class NovitaHttpChatRequest(
    val model: String? = null,
    val messages: List<NovitaHttpChatMessage>? = null,
    val max_tokens: Int? = null,
    val stream: Boolean? = false,
    val n: Int? = null,
    val seed: Int? = null,
    val frequency_penalty: Double? = null,
    val presence_penalty: Double? = null,
    val repetition_penalty: Double? = null,
    val stop: List<String>? = null,
    val temperature: Double? = null,
    val top_p: Double? = null,
    val top_k: Int? = null,
    val min_p: Double? = null,
    val response_format: NovitaHttpChatResponseFormat? = null,
)

@Serializable
data class NovitaHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class NovitaHttpChatResponseFormat(
    val type: String?,
    val json_schema: NovitaHttpJsonSchema? = null,
) {
    companion object {
        fun from(genericResponseFormat: ResponseFormat?): NovitaHttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null -> null
                ResponseFormat.Type.TEXT -> NovitaHttpChatResponseFormat("text", null)
                ResponseFormat.Type.JSON_OBJECT -> NovitaHttpChatResponseFormat("json_object")
                ResponseFormat.Type.JSON_SCHEMA ->
                    NovitaHttpChatResponseFormat(
                        "json_schema",
                        NovitaHttpJsonSchema("response_format", null, false, genericResponseFormat.schema),
                    )
            }
    }
}

@Serializable
data class NovitaHttpJsonSchema(
    val name: String?,
    val description: String?,
    val strict: Boolean?,
    val schema: JsonObject?,
)

@Serializable
data class NovitaHttpChatResponse(
    val choices: List<NovitaHttpChatResponseChoice>? = null,
    val id: String? = null,
    val model: String? = null,
    val usage: NovitaHttpUsage? = null,
)

@Serializable
data class NovitaHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: NovitaHttpChatMessage? = null,
)

@Serializable
data class NovitaHttpUsage(
    val completion_tokens: Int? = null,
    val prompt_tokens: Int? = null,
    val total_tokens: Int? = null,
)

@Serializable
data class NovitaHttpModelsResponse(
    val data: List<NovitaHttpModel>? = null,
)

@Serializable
data class NovitaHttpModel(
    val id: String? = null,
    val created: Long? = null,
    val title: String? = null,
    val display_name: String? = null,
    val description: String? = null,
    val context_size: Int? = null,
)
