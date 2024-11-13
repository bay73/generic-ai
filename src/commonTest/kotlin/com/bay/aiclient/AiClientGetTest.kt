package com.bay.aiclient

import com.bay.aiclient.api.ai21.Ai21Client
import com.bay.aiclient.api.anthropic.AnthropicClient
import com.bay.aiclient.api.bedrock.BedrockClient
import com.bay.aiclient.api.cerebras.CerebrasClient
import com.bay.aiclient.api.cohere.CohereClient
import com.bay.aiclient.api.google.GoogleClient
import com.bay.aiclient.api.mistral.MistralClient
import com.bay.aiclient.api.openai.OpenAiClient
import com.bay.aiclient.api.sambanova.SambaNovaClient
import com.bay.aiclient.api.togetherai.TogetherAiClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

class AiClientGetTest {
    @Test
    fun getAi21_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.AI21) {
                this.defaultModel = "test-ai21-model"
                this.defaultTemperature = 0.1
                this.timeout = 10.seconds
            }

        assertIs<Ai21Client>(client)
        assertEquals("test-ai21-model", client.defaultModel)
        assertEquals(0.1, client.defaultTemperature)
        assertEquals(10.seconds, client.timeout)
    }

    @Test
    fun getAnthropic_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.ANTHROPIC) {
                this.defaultModel = "test-anthropic-model"
                this.defaultTemperature = 0.2
                this.timeout = 20.seconds
            }

        assertIs<AnthropicClient>(client)
        assertEquals("test-anthropic-model", client.defaultModel)
        assertEquals(0.2, client.defaultTemperature)
        assertEquals(20.seconds, client.timeout)
    }

    @Test
    fun getCerebras_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.CEREBRAS) {
                this.defaultModel = "test-cerebras-model"
                this.defaultTemperature = 0.3
                this.timeout = 30.seconds
            }

        assertIs<CerebrasClient>(client)
        assertEquals("test-cerebras-model", client.defaultModel)
        assertEquals(0.3, client.defaultTemperature)
        assertEquals(30.seconds, client.timeout)
    }

    @Test
    fun getCohere_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.COHERE) {
                this.defaultModel = "test-cohere-model"
                this.defaultTemperature = 0.4
                this.timeout = 40.seconds
            }

        assertIs<CohereClient>(client)
        assertEquals("test-cohere-model", client.defaultModel)
        assertEquals(0.4, client.defaultTemperature)
        assertEquals(40.seconds, client.timeout)
    }

    @Test
    fun getGoogle_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.GOOGLE) {
                this.defaultModel = "test-google-model"
                this.defaultTemperature = 0.5
                this.timeout = 50.seconds
            }

        assertIs<GoogleClient>(client)
        assertEquals("test-google-model", client.defaultModel)
        assertEquals(0.5, client.defaultTemperature)
        assertEquals(50.seconds, client.timeout)
    }

    @Test
    fun getMistral_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.MISTRAL) {
                this.defaultModel = "test-mistral-model"
                this.defaultTemperature = 0.6
                this.timeout = 60.seconds
            }

        assertIs<MistralClient>(client)
        assertEquals("test-mistral-model", client.defaultModel)
        assertEquals(0.6, client.defaultTemperature)
        assertEquals(60.seconds, client.timeout)
    }

    @Test
    fun getOpenAi_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.OPEN_AI) {
                this.defaultModel = "test-gpt-model"
                this.defaultTemperature = 0.7
                this.timeout = 70.seconds
            }

        assertIs<OpenAiClient>(client)
        assertEquals("test-gpt-model", client.defaultModel)
        assertEquals(0.7, client.defaultTemperature)
        assertEquals(70.seconds, client.timeout)
    }

    @Test
    fun getSambaNova_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.SAMBA_NOVA) {
                this.defaultModel = "test-samba-model"
                this.defaultTemperature = 0.8
                this.timeout = 80.seconds
            }

        assertIs<SambaNovaClient>(client)
        assertEquals("test-samba-model", client.defaultModel)
        assertEquals(0.8, client.defaultTemperature)
        assertEquals(80.seconds, client.timeout)
    }

    @Test
    fun getTogether_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.TOGETHER_AI) {
                this.defaultModel = "test-together-model"
                this.defaultTemperature = 0.9
                this.timeout = 90.seconds
            }

        assertIs<TogetherAiClient>(client)
        assertEquals("test-together-model", client.defaultModel)
        assertEquals(0.9, client.defaultTemperature)
        assertEquals(90.seconds, client.timeout)
    }

    @Test
    fun getBedrock_createsRightClient() {
        val client =
            AiClient.get(AiClient.Type.BEDROCK) {
                this.defaultModel = "test-bedrock-model"
                this.defaultTemperature = 1.0
                this.timeout = 100.seconds
            }

        assertIs<BedrockClient>(client)
        assertEquals("test-bedrock-model", client.defaultModel)
        assertEquals(1.0, client.defaultTemperature)
        assertEquals(100.seconds, client.timeout)
    }

    @Test
    fun getWithClass_returnsRequestedClass() {
        val client =
            AiClient.get(CohereClient::class) {
                this.defaultModel = "test-model"
                this.defaultTemperature = 0.33
                this.timeout = 33.seconds
            }

        assertIs<CohereClient>(client)
        assertEquals("test-model", client.defaultModel)
        assertEquals(0.33, client.defaultTemperature)
        assertEquals(33.seconds, client.timeout)
    }
}
