package com.bay.aiclient.api.azureopenai

import com.bay.aiclient.domain.ResponseFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AzureOpenAiHttpChatRequest(
    val temperature: Double? = null,
    val top_p: Double? = null,
    val stream: Boolean? = false,
    val stop: List<String>? = null,
    val max_completion_tokens: Int? = null,
    val presence_penalty: Double? = null,
    val frequency_penalty: Double? = null,
    val messages: List<AzureOpenAiHttpChatMessage>? = null,
    val response_format: AzureOpenAiHttpChatResponseFormat? = null,
    val seed: Int? = null,
)

@Serializable
data class AzureOpenAiHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class AzureOpenAiHttpChatResponseFormat(
    val type: String?,
    val json_schema: AzureOpenAiHttpJsonSchema? = null,
) {
    companion object {
        fun from(genericResponseFormat: ResponseFormat?): AzureOpenAiHttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null -> null
                ResponseFormat.Type.TEXT -> AzureOpenAiHttpChatResponseFormat("text", null)
                ResponseFormat.Type.JSON_OBJECT -> AzureOpenAiHttpChatResponseFormat("json_object")
                ResponseFormat.Type.JSON_SCHEMA ->
                    AzureOpenAiHttpChatResponseFormat(
                        "json_schema",
                        AzureOpenAiHttpJsonSchema("response_format", null, false, genericResponseFormat.schema),
                    )
            }
    }
}

@Serializable
data class AzureOpenAiHttpJsonSchema(
    val name: String?,
    val description: String?,
    val strict: Boolean?,
    val schema: JsonObject?,
)

@Serializable
data class AzureOpenAiHttpChatResponse(
    val choices: List<AzureOpenAiHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: AzureOpenAiHttpUsage? = null,
)

@Serializable
data class AzureOpenAiHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: AzureOpenAiHttpChatMessage? = null,
)

@Serializable
data class AzureOpenAiHttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
    val completion_tokens_details: AzureOpenAiHttpCompletionTokenDetails? = null,
)

@Serializable
data class AzureOpenAiHttpCompletionTokenDetails(
    val reasoning_tokens: Int? = null,
)
