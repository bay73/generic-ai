package com.bay.aiclient.domain

import kotlinx.serialization.Serializable

@Serializable
abstract class ModelsResponse {
    abstract val models: List<Model>
}

@Serializable
abstract class Model {
    abstract val id: String
    abstract val name: String
}
