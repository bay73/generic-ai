package com.bay.aiclient.api.bedrock

actual class BedrockClientInternal actual constructor(
    credentials: BedrockClient.Credentials,
) {
    actual suspend fun models(): BedrockModelsResponse {
        TODO("Not yet implemented")
    }

    actual suspend fun generateText(request: BedrockGenerateTextRequest): BedrockGenerateTextResponse {
        TODO("Not yet implemented")
    }
}
