package com.bay.aiclient.api.mistral

import com.bay.aiclient.domain.GenerateTextRequest
import kotlinx.serialization.Serializable

@Serializable
data class MistralHttpChatRequest(
    val model: String? = null,
    val temperature: Double? = null,
    val top_p: Double? = null,
    val max_tokens: Int? = null,
    val stream: Boolean? = false,
    val stop: List<String>? = null,
    val random_seed: Int? = null,
    val messages: List<MistralHttpChatMessage>? = null,
    val response_format: MistralHttpChatResponseFormat? = null,
    val presence_penalty: Double? = null,
    val frequency_penalty: Double? = null,
)

@Serializable
data class MistralHttpChatMessage(
    val role: String? = null,
    val content: String? = null,
)

@Serializable
data class MistralHttpChatResponseFormat(
    val type: String?,
) {
    companion object {
        fun from(genericResponseFormat: GenerateTextRequest.ResponseFormat?): MistralHttpChatResponseFormat? =
            when (genericResponseFormat?.type) {
                null -> null
                GenerateTextRequest.ResponseFormat.Type.TEXT -> MistralHttpChatResponseFormat("text")
                GenerateTextRequest.ResponseFormat.Type.JSON_OBJECT, GenerateTextRequest.ResponseFormat.Type.JSON_SCHEMA ->
                    MistralHttpChatResponseFormat("json_object")
            }
    }
}

@Serializable
data class MistralHttpChatResponse(
    val choices: List<MistralHttpChatResponseChoice>? = null,
    val model: String? = null,
    val usage: MistralHttpUsage? = null,
)

@Serializable
data class MistralHttpChatResponseChoice(
    val finish_reason: String? = null,
    val index: Int? = null,
    val message: MistralHttpChatMessage? = null,
)

@Serializable
data class MistralHttpUsage(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
)

@Serializable
data class MistralHttpModelsResponse(
    val data: List<MistralHttpModel>? = null,
)

@Serializable
data class MistralHttpModel(
    val id: String? = null,
    val name: String? = null,
    val description: String? = null,
    val deprecation: String? = null,
    val created: Long? = null,
)
