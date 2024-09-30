package com.bay.aiclient.api.cohere

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class CohereModelsResponse(
    override val models: List<CohereModel>,
) : ModelsResponse()

@Serializable
data class CohereModel(
    override val id: String,
    override val name: String,
) : Model()
