package com.bay.aiclient

import com.bay.aiclient.api.ai21.Ai21Client
import com.bay.aiclient.api.anthropic.AnthropicClient
import com.bay.aiclient.api.azureopenai.AzureOpenAiClient
import com.bay.aiclient.api.bedrock.BedrockClient
import com.bay.aiclient.api.cerebras.CerebrasClient
import com.bay.aiclient.api.cohere.CohereClient
import com.bay.aiclient.api.deepseek.DeepSeekClient
import com.bay.aiclient.api.google.GoogleClient
import com.bay.aiclient.api.grok.GrokClient
import com.bay.aiclient.api.mistral.MistralClient
import com.bay.aiclient.api.openai.OpenAiClient
import com.bay.aiclient.api.sambanova.SambaNovaClient
import com.bay.aiclient.api.togetherai.TogetherAiClient
import kotlin.test.Test
import kotlin.test.assertIs

class AiClientGetBuilderTest {
    @Test
    fun getAi21_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.AI21)

        assertIs<Ai21Client.Builder>(builder)
    }

    @Test
    fun getAnthropic_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.ANTHROPIC)

        assertIs<AnthropicClient.Builder>(builder)
    }

    @Test
    fun getAzureOpenAi_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.AZURE_OPENAI)

        assertIs<AzureOpenAiClient.Builder>(builder)
    }

    @Test
    fun getCerebras_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.CEREBRAS)

        assertIs<CerebrasClient.Builder>(builder)
    }

    @Test
    fun getCohere_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.COHERE)

        assertIs<CohereClient.Builder>(builder)
    }

    @Test
    fun getDeepSeek_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.DEEP_SEEK)

        assertIs<DeepSeekClient.Builder>(builder)
    }

    @Test
    fun getGoogle_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.GOOGLE)

        assertIs<GoogleClient.Builder>(builder)
    }

    @Test
    fun getGrok_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.GROK)

        assertIs<GrokClient.Builder>(builder)
    }

    @Test
    fun getMistral_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.MISTRAL)

        assertIs<MistralClient.Builder>(builder)
    }

    @Test
    fun getOpenAi_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.OPEN_AI)

        assertIs<OpenAiClient.Builder>(builder)
    }

    @Test
    fun getSambaNova_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.SAMBA_NOVA)

        assertIs<SambaNovaClient.Builder>(builder)
    }

    @Test
    fun getTogether_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.TOGETHER_AI)

        assertIs<TogetherAiClient.Builder>(builder)
    }

    @Test
    fun getBedrock_createsRightBuilder() {
        val builder = AiClient.getBuilder(AiClient.Type.BEDROCK)

        assertIs<BedrockClient.Builder>(builder)
    }
}
