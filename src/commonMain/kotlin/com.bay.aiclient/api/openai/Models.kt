package com.bay.aiclient.api.openai

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiModelsResponse(
    override val models: List<OpenAiModel>,
) : ModelsResponse()

@Serializable
data class OpenAiModel(
    override val id: String,
    override val name: String,
    val created: Long?,
) : Model()
