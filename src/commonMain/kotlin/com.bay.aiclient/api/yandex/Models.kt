package com.bay.aiclient.api.yandex

import com.bay.aiclient.domain.Model
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.serialization.Serializable

@Serializable
data class YandexModelsResponse(
    override val models: List<YandexModel>,
) : ModelsResponse()

@Serializable
data class YandexModel(
    override val id: String,
    override val name: String,
) : Model()
