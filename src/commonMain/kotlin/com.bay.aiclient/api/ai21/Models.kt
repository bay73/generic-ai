package com.bay.aiclient.api.ai21

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class Ai21ModelsResponse(
    override val models: List<Ai21Model>,
) : ModelsResponse()

@Serializable
data class Ai21Model(
    override val id: String,
    override val name: String,
) : Model()
