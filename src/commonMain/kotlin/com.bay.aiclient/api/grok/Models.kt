package com.bay.aiclient.api.grok

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class GrokModelsResponse(
    override val models: List<GrokModel>,
) : ModelsResponse()

@Serializable
data class GrokModel(
    override val id: String,
    override val name: String,
    val created: Long?,
) : Model()
