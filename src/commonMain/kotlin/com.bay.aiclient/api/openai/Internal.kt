package com.bay.aiclient.api.openai

import com.bay.aiclient.domain.ResponseFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OpenAiHttpChatRequest(
    val messages: List<OpenAiHttpChatMessage>? = null,
    val model: String? = null,
    val frequency_penalty: Double? = null,
    val max_completion_tokens: Int? = null,
    val presence_penalty: Double? = null,
    val response_format: OpenAiHttpChatResponseFormat? = null,
    val seed: Int? = null,
    val stop: List<String>? = null,
    val stream: Boolean? = false,
    val temperature: Double? = null,
    val top_p: Double? = null,
)

@Serializable
data class OpenAiHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class OpenAiHttpChatResponseFormat(
    val type: String?,
    val json_schema: OpenAiHttpJsonSchema? = null,
) {
    companion object {
        fun from(genericResponseFormat: ResponseFormat?): OpenAiHttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null -> null
                ResponseFormat.Type.TEXT -> OpenAiHttpChatResponseFormat("text", null)
                ResponseFormat.Type.JSON_OBJECT -> OpenAiHttpChatResponseFormat("json_object")
                ResponseFormat.Type.JSON_SCHEMA ->
                    OpenAiHttpChatResponseFormat(
                        "json_schema",
                        OpenAiHttpJsonSchema("response_format", null, false, genericResponseFormat.schema),
                    )
            }
    }
}

@Serializable
data class OpenAiHttpJsonSchema(
    val name: String?,
    val description: String?,
    val strict: Boolean?,
    val schema: JsonObject?,
)

@Serializable
data class OpenAiHttpChatResponse(
    val choices: List<OpenAiHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: OpenAiHttpUsage? = null,
)

@Serializable
data class OpenAiHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: OpenAiHttpChatMessage? = null,
)

@Serializable
data class OpenAiHttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
    val completion_tokens_details: OpenAiHttpCompletionTokenDetails? = null,
)

@Serializable
data class OpenAiHttpCompletionTokenDetails(
    val reasoning_tokens: Int? = null,
)

@Serializable
data class OpenAiHttpModelsResponse(
    val data: List<OpenAiHttpModel>? = null,
)

@Serializable
data class OpenAiHttpModel(
    val id: String? = null,
    val created: Long? = null,
)
