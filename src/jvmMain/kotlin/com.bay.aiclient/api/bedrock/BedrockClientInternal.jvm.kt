package com.bay.aiclient.api.bedrock

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrock.BedrockClient
import software.amazon.awssdk.services.bedrock.model.ListFoundationModelsRequest
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration
import software.amazon.awssdk.services.bedrockruntime.model.Message
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock
import kotlin.time.Duration
import kotlin.time.toJavaDuration

actual class BedrockClientInternal actual constructor(
    credentials: com.bay.aiclient.api.bedrock.BedrockClient.Credentials,
    timeout: Duration,
) {
    private val region = Region.of(credentials.region)

    private val credentialsProvider =
        if (credentials.useDefault) {
            DefaultCredentialsProvider.create()
        } else {
            StaticCredentialsProvider.create(
                AwsSessionCredentials
                    .builder()
                    .accessKeyId(credentials.accessKeyId)
                    .secretAccessKey(credentials.secretAccessKey)
                    .sessionToken(credentials.sessionToken)
                    .build(),
            )
        }

    private val bedrockClient =
        BedrockClient
            .builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .overrideConfiguration {
                it.apiCallTimeout(timeout.toJavaDuration())
            }.build()

    private val bedrockRuntimeClient =
        BedrockRuntimeClient
            .builder()
            .credentialsProvider(credentialsProvider)
            .region(region)
            .overrideConfiguration {
                it.apiCallTimeout(timeout.toJavaDuration())
            }.build()

    actual suspend fun models(): BedrockModelsResponse {
        val modelsResponse = bedrockClient.listFoundationModels(ListFoundationModelsRequest.builder().build())
        return BedrockModelsResponse(
            modelsResponse.modelSummaries()?.map { BedrockModel(it.modelId(), it.modelName()) } ?: emptyList(),
        )
    }

    actual suspend fun generateText(request: BedrockGenerateTextRequest): BedrockGenerateTextResponse {
        val historyMessages =
            request.chatHistory?.map {
                Message
                    .builder()
                    .role(it.role)
                    .content(ContentBlock.fromText(it.content))
                    .build()
            } ?: emptyList()
        val newMessage =
            listOf(
                Message
                    .builder()
                    .role(ConversationRole.USER)
                    .content(ContentBlock.fromText(request.prompt))
                    .build(),
            )

        val inferenceConfig = InferenceConfiguration.builder()
        if (request.maxOutputTokens != null) {
            inferenceConfig.maxTokens(request.maxOutputTokens)
        }
        if (request.stopSequences != null) {
            inferenceConfig.stopSequences(request.stopSequences)
        }
        if (request.temperature != null) {
            inferenceConfig.temperature(request.temperature.toFloat())
        }
        if (request.topP != null) {
            inferenceConfig.topP(request.topP.toFloat())
        }
        val converseRequest =
            ConverseRequest
                .builder()
                .modelId(request.model)
                .messages(historyMessages + newMessage)
                .inferenceConfig(inferenceConfig.build())
        if (!request.systemInstructions.isNullOrBlank()) {
            converseRequest.system(SystemContentBlock.fromText(request.systemInstructions))
        }
        val converseResponse = bedrockRuntimeClient.converse(converseRequest.build())
        return BedrockGenerateTextResponse(
            response =
                converseResponse
                    .output()
                    .message()
                    .content()
                    .firstOrNull()
                    ?.text(),
            usage =
                BedrockGenerateTextTokenUsage(
                    inputTokens = converseResponse.usage().inputTokens(),
                    outputTokens = converseResponse.usage().outputTokens(),
                    totalTokens = converseResponse.usage().totalTokens(),
                ),
        )
    }
}
