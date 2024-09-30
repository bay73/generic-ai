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
    implementation("com.bay:generic-ai:0.2.1")
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
## 💡 Sample application

Sample multiplatform application using the library is available at [GitHub](https://github.com/bay73/generic-ai-ktm-demo)

## 📄 License

Generic AI Kotlin API Client is an open-sourced software licensed under the [MIT license](LICENSE).
**Please note that this is an unofficial library and is not affiliated with or endorsed by any AI provider**. Contributions are always welcome!
