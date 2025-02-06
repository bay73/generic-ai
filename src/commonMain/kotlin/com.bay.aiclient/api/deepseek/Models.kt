package com.bay.aiclient.api.deepseek

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekModelsResponse(
    override val models: List<DeepSeekModel>,
) : ModelsResponse()

@Serializable
data class DeepSeekModel(
    override val id: String,
    override val name: String,
) : Model()
