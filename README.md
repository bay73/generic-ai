# Generic AI client for Kotlin

[![License](https://img.shields.io/github/license/bay73/generic-ai?color=red)](LICENSE)

Easy-to-use generic Kotlin API client for connecting to various AI providers with multiplatform support.

## 🛠️ Setup

1. To install the Generic-AI Kotlin client, add this dependency to your build.gradle file:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.bay73:generic-ai:0.2.1")
}
```

### Multiplatform

In multiplatform projects, include the generic-AI client dependency in `commonMain` and select a [specific engine](https://ktor.io/docs/http-client-engines.html) for each target.

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

## 💡 Sample application

Sample multiplatform application using the library is available at [GitHub](https://github.com/bay73/generic-ai-ktm-demo)

## 📄 License

Generic AI Kotlin API Client is an open-sourced software licensed under the [MIT license](LICENSE).
**Please note that this is an unofficial library and is not affiliated with or endorsed by any AI provider**. Contributions are always welcome!
