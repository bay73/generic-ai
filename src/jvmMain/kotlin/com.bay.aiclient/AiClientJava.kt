package com.bay.aiclient

import com.bay.aiclient.domain.GenerateTextRequest
import com.bay.aiclient.domain.GenerateTextResponse
import com.bay.aiclient.domain.ModelsResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

class AiClientJava(
    val internalClient: AiClient,
) {
    fun models(): CompletableFuture<Result<ModelsResponse>> = GlobalScope.future { internalClient.models() }

    fun generateText(request: GenerateTextRequest): CompletableFuture<Result<GenerateTextResponse>> =
        GlobalScope.future {
            internalClient.generateText(request)
        }
}
