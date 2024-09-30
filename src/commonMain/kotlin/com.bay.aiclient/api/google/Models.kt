package com.bay.aiclient.api.google

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class GoogleModelsResponse(
    override val models: List<GoogleModel>,
) : ModelsResponse()

@Serializable
data class GoogleModel(
    override val id: String,
    override val name: String,
) : Model()
