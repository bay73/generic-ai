package com.bay.aiclient.api.mistral

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class MistralModelsResponse(
    override val models: List<MistralModel>,
) : ModelsResponse()

@Serializable
data class MistralModel(
    override val id: String,
    override val name: String,
    val description: String?,
    val deprecation: String?,
    val created: Long?,
) : Model()
