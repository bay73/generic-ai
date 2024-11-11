package com.bay.aiclient.api.togetherai

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class TogetherAiModelsResponse(
    override val models: List<TogetherAiModel>,
) : ModelsResponse()

@Serializable
data class TogetherAiModel(
    override val id: String,
    override val name: String,
    val type: String?,
    val created: Long?,
) : Model()
