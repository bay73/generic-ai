# Generic AI client for Kotlin

[![License](https://img.shields.io/github/license/bay73/generic-ai?color=red)](LICENSE)

Easy-to-use generic Kotlin API client for connecting to various AI providers with multiplatform support. Supported AI providers:
- [AI21 Lab](https://www.ai21.com/)
- [Anthropic](https://docs.anthropic.com/en/api/getting-started)
- [AWS Bedrock](https://docs.aws.amazon.com/bedrock/)
- [Azure OpenAI](https://learn.microsoft.com/en-us/azure/ai-services/openai/)
- [Cerebras](https://inference-docs.cerebras.ai/introduction)
- [Cohere](https://docs.cohere.com/)
- [DeepSeek](https://platform.deepseek.com/)
- [Google Gemini](https://ai.google.dev/gemini-api/docs)
- [Grok](https://docs.x.ai/docs/api-reference)
- [Inception Labs] (https://platform.inceptionlabs.ai/docs)
- [Mistral](https://docs.mistral.ai/)
- [OpenAI](https://platform.openai.com/docs/overview)
- [SambaNova](https://community.sambanova.ai/docs)
- [Together AI](https://docs.together.ai/docs)
- [Yandex AI Studio](https://yandex.cloud/en/docs/foundation-models/text-generation/api-ref/TextGeneration/completion)

## 🛠️ Setup

1. To install the Generic-AI Kotlin client, add this dependency to your build.gradle file:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.bay73:generic-ai:0.6.3")
}
```

Generic-AI uses ktor library to work with http requests so you need to include ktor client corresponding to your platform.

### Multiplatform

In multiplatform projects, include the generic-AI client dependency in `commonMain` and select a [specific ktor engine](https://ktor.io/docs/http-client-engines.html) for each target.

## 🚀 Basic usage

```kotlin
import com.bay.aiclient.AiClient

fun getResponse() {
    // Create a client for specified AI provider.
    val client = AiClient.get(AiClient.Type.OPEN_AI) { // Choose provider
        apiKey = "put your API key here"
        defaultModel = "gpt-4o-mini"    // Choose model
    }
    // Start request to the AI model.
    val job = client.generateText { prompt = "When the first LLM was created?" }
    // Wait for execution and get response.
    println(job.await().getOrThrow().response)
    
    // Get list of available models
    val models = client.models()
    models.models.forEach { println(it.id) }  
}

```

### Usage in Java
The library created to use in Kotlin code and so usage in Java is cumbersome while possible.
Special `AiClientJava` wrapper is created to use CompletableFuture for asynchronous execution.
Here is a code example:

```java
import com.bay.aiclient.AiClient;
import com.bay.aiclient.AiClientJava;
import com.bay.aiclient.domain.GenerateTextRequest;
import com.bay.aiclient.domain.GenerateTextResponse;

getResponse main() throws ExecutionException, InterruptedException {
    AiClient.Builder clientBuilder = AiClient.Companion.getBuilder(AiClient.Type.OPEN_AI); // Choose provider
    clientBuilder.setApiKey("put your API key here");
    clientBuilder.setDefaultModel("gpt-4o-mini");       // Choose model
    AiClient client = clientBuilder.build();
    AiClientJava javaClient = new AiClientJava(client);  // Java client uses CompletableFuture

    // Start request to the AI model.
    GenerateTextRequest.Builder requestBuilder = client.textGenerationRequestBuilder();
    requestBuilder.setPrompt("When the first LLM were released?");
    CompletableFuture<Result<GenerateTextResponse>> response = javaClient.generateText(requestBuilder.build());

    // Wait for execution and get response.
    response.join();
    if (response.isDone()) {
        System.out.println(response.get());
    }
}

```
## 🔧 Generic settings
There are set of generic settings which can be used for any AI provider to customize model behavior for a specific request.

```kotlin
fun customizeRequest() {
    val response = cleint.generateText {
        model = "model_id" // Model id to use for a specific request.
        prompt = "" // User prompt which initiates generation 
        systemInstructions = "" // Additional system instruction to adjust AI behavior.
        responseFormat = ResponseFormat.JSON_OBJECT // Allows to specify response format. See details below.
        chatHistory = listOf<TextMessage>() // A list of chat messages in chronological order, representing a conversation between the user and the model.
        maxOutputTokens = 100 // The maximum number of tokens that can be generated as part of the response.
        stopSequences = listOf<String>() // A list of strings that the model uses to stop generation.
        temperature = 0.1 // A non-negative float that tunes the degree of randomness in generation. Lower temperatures mean less random generations.
        topP = 0.5 // An alternative way controlling the diversity of the model's responses. It's recommended to use either temperature or topP.
    }
}
```

`responseFormat` allows to specify returning text, generic JSON or validated JSON schema. The parameter has limited support by different providers:  

| Provider       | Text | Generic JSON | JSON schema          |
|----------------|------|--------------|----------------------|
| AI21 Lab       | ✅    | ✅            |                      |
| Anthropic      | ✅    |              |                      |
| AWS Bedrock    | ✅    |              |                      |
| Azure OpenAI   | ✅    | ✅            | ✅                    |
| Cerebras       | ✅    | ✅            |                      |
| Cohere         | ✅    | ✅            | ✅                    |
| DeepSeek       | ✅    | ✅            |                      |
| Google Gemini  | ✅    | ✅            | ✅                    |
| Grok           | ✅    | ✅            | ✅                    |
| Inception Labs | ✅    |              |                      |
| Mistral        | ✅    | ✅            |                      |
| OpenAI         | ✅    | ✅            | ✅ depending on model |
| SambaNova      | ✅    |              |                      |
| Together AI    | ✅    | ✅            | ✅ depending on model |
| Yandex AI      | ✅    | ✅            | ✅                    |

   


## ⚙️ Provider specific settings

### Additional generation parameters

Some AI providers have additional settings which can be adjusted. To use this you need to request client of a specific class: 
```kotlin
fun getResponseWithSpecificParameters() {
    // Create a client for specified AI provider.
    val client = AiClient.get(CohereClient::class) { // Choose provider
        apiKey = "put your API key here"
        defaultModel = "command-r"    // Choose model
    }
    // Start request to the AI model.
    val response = client.generateText { 
        prompt = "When the first LLM was created?"
        seed = 5  // see provider documentation for usage of specific parameters 
        frequencyPenalty = 0.1
    }
}

```

### AWS Bedrock authentication

AWS bedrock doesn't support token based authentication. To use it you need to provide special credentials object to the client constructor:
```kotlin
fun getBedrockClient() {
    // You can use either [DefaultCredentialsProvider](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html) or specify access key and token directly.
    val credentials = BedrockClient.Credentials("region", true)
    // or 
    val credentials = BedrockClient.Credentials("region", false, "accessKeyId", "secretAccessKey", "sessionToken ")

    // Create a client using the credentials provider
    val client = BedrockClient.Builder(creadentals).build()
}

```

### Azure OpenAI connection

Azure OpenAI requires the resource name which is used to access services as a part of endpoint. To specify resource name use dedicated client constructor which allow to set the resource name as a string:
```kotlin
fun getAzureOpenAiClient() {
    // Create a client using specific builder
    val client = AzureOpenAiClient.Builder().apply {
        resourceName = "your-resource-name"
        apiKey = "your-api-key"
    }.build()
}
```
Further usage of this client is the same as for all other AI clients.

### Yandex AI Studio connection

Yandex requires to specify a folder - a space where Yandex Cloud resources are created and grouped. It is used as a part of foundation model URI. To specify the folder use dedicated client builder which allow to set the resource folder as a string:
```kotlin
fun getYandexOpenAiClient() {
    // Create a client using specific builder
    val client = YandexOpenAiClient.Builder().apply {
        resourceFolder = "your-resource-name"
        apiKey = "your-api-key"
    }.build()
}
```
Further usage of this client is the same as for all other AI clients.

## 💡 Sample application

Sample multiplatform application using the library is available at [GitHub](https://github.com/bay73/generic-ai-ktm-demo)

## 📄 License

Generic AI Kotlin API Client is an open-sourced software licensed under the [MIT license](LICENSE).
**Please note that this is an unofficial library and is not affiliated with or endorsed by any AI provider**. Contributions are always welcome!
