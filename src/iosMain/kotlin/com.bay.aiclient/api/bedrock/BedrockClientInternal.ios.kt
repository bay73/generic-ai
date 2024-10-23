package com.bay.aiclient.api.bedrock

import kotlin.time.Duration

actual class BedrockClientInternal actual constructor(
    credentials: BedrockClient.Credentials,
    timeout: Duration,
) {
    actual suspend fun models(): BedrockModelsResponse {
        TODO("Not yet implemented")
    }

    actual suspend fun generateText(request: BedrockGenerateTextRequest): BedrockGenerateTextResponse {
        TODO("Not yet implemented")
    }
}
