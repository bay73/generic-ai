package com.bay.aiclient.api.anthropic

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class AnthropicModelsResponse(
    override val models: List<AnthropicModel>,
) : ModelsResponse()

@Serializable
data class AnthropicModel(
    override val id: String,
    override val name: String,
) : Model()
