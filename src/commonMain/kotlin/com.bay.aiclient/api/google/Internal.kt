package com.bay.aiclient.api.google

import kotlinx.serialization.Serializable

@Serializable
data class GoogleHttpChatRequest(
    val contents: List<GoogleHttpChatContent>? = null,
    val systemInstruction: GoogleHttpChatContent? = null,
    val generationConfig: GoogleHttpGenerationConfig? = null,
)

@Serializable
data class GoogleHttpChatContent(
    val role: String? = null,
    val parts: List<GoogleMessageTextPart>? = null,
)

@Serializable
data class GoogleMessageTextPart(
    val text: String? = null,
)

@Serializable
data class GoogleHttpGenerationConfig(
    val stopSequences: List<String>? = null,
    val maxOutputTokens: Int? = null,
    val temperature: Double? = null,
    val topP: Double? = null,
    val topK: Double? = null,
    val presencePenalty: Double? = null,
    val frequencyPenalty: Double? = null,
)

@Serializable
data class GoogleHttpChatResponse(
    val candidates: List<GoogleHttpChatResponseCandidate>? = null,
    val usageMetadata: GoogleUsageMetadata? = null,
)

@Serializable
data class GoogleHttpChatResponseCandidate(
    val content: GoogleHttpChatContent? = null,
    val finishReason: String? = null,
    val index: Int? = null,
)

@Serializable
data class GoogleUsageMetadata(
    val promptTokenCount: Int? = null,
    val cachedContentTokenCount: Int? = null,
    val candidatesTokenCount: Int? = null,
    val totalTokenCount: Int? = null,
)

@Serializable
data class GoogleHttpModelsResponse(
    val models: List<GoogleHttpModel>? = null,
)

@Serializable
data class GoogleHttpModel(
    val name: String? = null,
    val displayName: String? = null,
    val description: String? = null,
)
