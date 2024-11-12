package com.bay.aiclient.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TextMessage(
    val role: String?,
    val content: String?,
)

@Serializable
abstract class GenerateTextRequest {
    abstract val model: String
    abstract val prompt: String
    abstract val systemInstructions: String?
    abstract val responseFormat: ResponseFormat?
    abstract val chatHistory: List<TextMessage>?
    abstract val maxOutputTokens: Int?
    abstract val stopSequences: List<String>?
    abstract val temperature: Double?
    abstract val topP: Double?

    open class Builder(
        open var prompt: String = "",
        open var model: String? = null,
        open var systemInstructions: String? = null,
        open var responseFormat: ResponseFormat? = null,
        open var chatHistory: List<TextMessage>? = null,
        open var maxOutputTokens: Int? = null,
        open var stopSequences: List<String>? = null,
        open var temperature: Double? = null,
        open var topP: Double? = null,
    ) {
        open fun build(): GenerateTextRequest =
            model?.let {
                GenericGenerateTextRequest(
                    it,
                    prompt,
                    systemInstructions,
                    responseFormat,
                    chatHistory,
                    maxOutputTokens,
                    stopSequences,
                    temperature,
                    topP,
                )
            }
                ?: throw IllegalStateException("Model should be set!")
    }

    fun <T : Builder> copyTo(builder: T) {
        builder.model = this.model
        builder.prompt = this.prompt
        this.systemInstructions?.also { builder.systemInstructions = it }
        this.chatHistory?.also { builder.chatHistory = it }
        this.maxOutputTokens?.also { builder.maxOutputTokens = it }
        this.stopSequences?.also { builder.stopSequences = it }
        this.temperature?.also { builder.temperature = it }
        this.topP?.also { builder.topP = it }
    }
}

@Serializable
data class ResponseFormat(
    val type: Type? = Type.TEXT,
    val schema: JsonObject? = null,
) {
    enum class Type {
        TEXT,
        JSON_OBJECT,
        JSON_SCHEMA,
    }

    companion object {
        val TEXT = ResponseFormat(Type.TEXT)
        val JSON_OBJECT = ResponseFormat(Type.JSON_OBJECT)
        fun JSON_SCHEMA(schema: JsonObject) = ResponseFormat(Type.JSON_SCHEMA, schema)
    }
}

@Serializable
data class GenericGenerateTextRequest(
    override val model: String = "",
    override val prompt: String = "",
    override val systemInstructions: String? = null,
    override val responseFormat: ResponseFormat? = null,
    override val chatHistory: List<TextMessage>? = null,
    override val maxOutputTokens: Int? = null,
    override val stopSequences: List<String>? = null,
    override val temperature: Double? = null,
    override val topP: Double? = null,
) : GenerateTextRequest()

@Serializable
abstract class GenerateTextResponse {
    abstract val response: String?
    abstract val usage: GenerateTextTokenUsage?
}

@Serializable
abstract class GenerateTextTokenUsage {
    abstract val inputToken: Int?
    abstract val outputToken: Int?
    abstract val totalToken: Int?
}
