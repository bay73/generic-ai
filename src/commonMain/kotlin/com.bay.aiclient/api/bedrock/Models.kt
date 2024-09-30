package com.bay.aiclient.api.bedrock

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class BedrockModelsResponse(
    override val models: List<BedrockModel>,
) : ModelsResponse()

@Serializable
data class BedrockModel(
    override val id: String,
    override val name: String,
) : Model()
