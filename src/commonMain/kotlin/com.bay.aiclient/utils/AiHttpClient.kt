package com.bay.aiclient.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration

class AiHttpClient(
    baseUrl: String,
    timeout: Duration,
    logLevel: LogLevel = LogLevel.NONE,
    httpEngine: HttpClientEngine? = null,
    authHeader: HeadersBuilder.() -> Unit,
) {
    suspend inline fun <reified Request, reified Response, reified T> runPost(
        path: String,
        request: Request,
        responseProcess: (T) -> Response,
    ): Result<Response> =
        runCatching {
            val response =
                client
                    .post(path) {
                        contentType(ContentType.Application.Json)
                        setBody(request)
                    }.body<T>()
            responseProcess(response)
        }

    suspend inline fun <reified Response, reified T> runGet(
        path: String,
        responseProcess: (T) -> Response,
    ): Result<Response> =
        runCatching {
            val response =
                client
                    .get(path) {
                        contentType(ContentType.Application.Json)
                    }.body<T>()
            responseProcess(response)
        }

    val client =
        HttpClient(engine = httpEngine ?: HttpClient().engine) {
            expectSuccess = true
            defaultRequest {
                url(baseUrl)
                headers {
                    append(HttpHeaders.Accept, "application/json")
                    append(HttpHeaders.ContentType, "application/json")
                    authHeader.invoke(this)
                }
            }
            install(Logging) {
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            println("[AI client ktor] $message")
                        }
                    }
                level = logLevel
                sanitizeHeader { header -> header == HttpHeaders.Authorization || header == "x-goog-api-key" }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = timeout.inWholeMilliseconds
                socketTimeoutMillis = timeout.inWholeMilliseconds
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                    },
                )
            }
        }
}
