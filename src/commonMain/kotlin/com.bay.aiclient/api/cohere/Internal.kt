package com.bay.aiclient.api.cohere

import com.bay.aiclient.domain.GenerateTextRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class CohereHttpChatRequest(
    val model: String? = null,
    val message: String,
    val stream: Boolean = false,
    val response_format: CohereHttpChatResponseFormat? = null,
    val preamble: String? = null,
    val chat_history: List<CohereHttpChatMessage>? = null,
    val temperature: Double? = null,
    val max_tokens: Int? = null,
    val max_input_tokens: Int? = null,
    val k: Int? = null,
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
data class CohereHttpChatResponseFormat(
    val type: String?,
    val json_schema: JsonObject? = null,
) {
    companion object {
        fun from(genericResponseFormat: GenerateTextRequest.ResponseFormat?): CohereHttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null -> null
                GenerateTextRequest.ResponseFormat.Type.TEXT -> CohereHttpChatResponseFormat("text")
                GenerateTextRequest.ResponseFormat.Type.JSON_OBJECT -> CohereHttpChatResponseFormat("json_object")
                GenerateTextRequest.ResponseFormat.Type.JSON_SCHEMA ->
                    CohereHttpChatResponseFormat(
                        "json_object",
                        genericResponseFormat.schema,
                    )
            }
    }
}

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
