package com.bay.aiclient.api.cerebras

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class CerebrasModelsResponse(
    override val models: List<CerebrasModel>,
) : ModelsResponse()

@Serializable
data class CerebrasModel(
    override val id: String,
    override val name: String,
    val created: Long?,
) : Model()
