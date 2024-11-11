# Generic AI client for Kotlin

[![License](https://img.shields.io/github/license/bay73/generic-ai?color=red)](LICENSE)

Easy-to-use generic Kotlin API client for connecting to various AI providers with multiplatform support. Supported AI providers:
- [AI21 Lab](https://www.ai21.com/)
- [AWS Bedrock](https://docs.aws.amazon.com/bedrock/)
- [Cohere](https://docs.cohere.com/)
- [Google Gemini](https://ai.google.dev/gemini-api/docs)
- [Mistral](https://docs.mistral.ai/)
- [OpenAI](https://platform.openai.com/docs/overview)
- [SambaNova](https://community.sambanova.ai/docs)

## 🛠️ Setup

1. To install the Generic-AI Kotlin client, add this dependency to your build.gradle file:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.bay73:generic-ai:0.3.1")
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
        apiAky = "put your API key here"
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
    clientBuilder.setApiAky("put your API key here");
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
        systemInstructions = "" // Additional system instruction to adjust AI behavior.
        chatHistory = listOf() // A list of chat messages in chronological order, representing a conversation between the user and the model.
        maxOutputTokens = 100 // The maximum number of tokens that can be generated as part of the response.
        stopSequences = listOf() // A list of strings that the model uses to stop generation.
        temperature = 0.1 // A non-negative float that tunes the degree of randomness in generation. Lower temperatures mean less random generations.
        topP = 0.5 // An alternative way controlling the diversity of the model's responses. It's recommended to use either temperature or topP.
    }
}
```

## ⚙️ Provider specific settings

### Additonal generation parameters

Some AI providers have additional settings which can be adjusted. To use this you need to request client of specific class: 
```kotlin
fun getResponseWithSpecificParameters() {
    // Create a client for specified AI provider.
    val client = AiClient.get(CohereClient::class) { // Choose provider
        apiAky = "put your API key here"
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
    // You can use either [DefaultCredentialsProvider](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html) or specify accesskey and token directly.
    val credentials = BedrockClient.Credentials("region", true)
    // or 
    val credentials = BedrockClient.Credentials("region", false, "accessKeyId", "secretAccessKey", "sessionToken ")
    
    // Create a client using the credentials provider
    val client = BedrockClient.Builder(creadentals).build()
}
    
```

## 💡 Sample application

Sample multiplatform application using the library is available at [GitHub](https://github.com/bay73/generic-ai-ktm-demo)

## 📄 License

Generic AI Kotlin API Client is an open-sourced software licensed under the [MIT license](LICENSE).
**Please note that this is an unofficial library and is not affiliated with or endorsed by any AI provider**. Contributions are always welcome!
