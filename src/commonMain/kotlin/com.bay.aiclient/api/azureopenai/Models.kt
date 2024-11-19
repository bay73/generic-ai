package com.bay.aiclient.api.azureopenai

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class AzureOpenAiModelsResponse(
    override val models: List<AzureOpenAiModel>,
) : ModelsResponse()

@Serializable
data class AzureOpenAiModel(
    override val id: String,
    override val name: String,
) : Model()
