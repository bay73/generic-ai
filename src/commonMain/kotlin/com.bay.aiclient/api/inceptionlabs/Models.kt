package com.bay.aiclient.api.inceptionlabs

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class InceptionLabsModelsResponse(
    override val models: List<InceptionLabsModel>,
) : ModelsResponse()

@Serializable
data class InceptionLabsModel(
    override val id: String,
    override val name: String,
) : Model()
