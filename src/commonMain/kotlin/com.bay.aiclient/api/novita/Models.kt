package com.bay.aiclient.api.novita

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class NovitaModelsResponse(
    override val models: List<NovitaModel>,
) : ModelsResponse()

@Serializable
data class NovitaModel(
    override val id: String,
    override val name: String,
    val created: Long?,
    val description: String?,
    val contextSize: Int?,
) : Model()
