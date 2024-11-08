package com.bay.aiclient.api.sambanova

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class SambaNovaModelsResponse(
    override val models: List<SambaNovaModel>,
) : ModelsResponse()

@Serializable
data class SambaNovaModel(
    override val id: String,
    override val name: String,
) : Model()
