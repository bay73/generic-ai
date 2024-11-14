package com.bay.aiclient.utils

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlin.test.assertEquals

class MockHttpEngine {
    private val requestMatchers = mutableListOf<RequestMatcher>()

    fun expect(requestMatcher: RequestMatcher): MockHttpEngine {
        requestMatchers.add(requestMatcher)
        return this
    }

    fun andRespondOk(jsonContent: String): MockEngine =
        MockEngine { request ->
            try {
                requestMatchers.forEach { it.match(request) }
            } catch (e: AssertionError) {
                println("java.lang.AssertionError: HTTP request assertion failed.")
                e.printStackTrace()
                throw e
            }
            respond(
                content = ByteReadChannel(jsonContent),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
}

interface RequestMatcher {
    suspend fun match(request: HttpRequestData)

    companion object {
        val WHITESPACE_REGEX = "[\\p{Zs}\r\n]+".toRegex()

        fun postRequestTo(expectedUrl: String) =
            object : RequestMatcher {
                override suspend fun match(request: HttpRequestData) {
                    assertEquals(HttpMethod.Post, request.method, "Request method")
                    assertEquals(expectedUrl, request.url.toString(), "Request URL")
                }
            }

        fun getRequestTo(expectedUrl: String) =
            object : RequestMatcher {
                override suspend fun match(request: HttpRequestData) {
                    assertEquals(HttpMethod.Get, request.method, "Request method")
                    assertEquals(expectedUrl, request.url.toString(), "Request URL")
                }
            }

        fun jsonBody(expectedBody: String) =
            object : RequestMatcher {
                override suspend fun match(request: HttpRequestData) {
                    assertEquals(
                        expectedBody.replace(WHITESPACE_REGEX, ""),
                        request.body
                            .toByteArray()
                            .decodeToString()
                            .replace(WHITESPACE_REGEX, ""),
                        "Request body",
                    )
                }
            }

        fun header(
            expectedHeader: String,
            expectedValue: String,
        ) = object : RequestMatcher {
            override suspend fun match(request: HttpRequestData) {
                assertEquals(expectedValue, request.headers[expectedHeader], "Request header $expectedHeader")
            }
        }
    }
}
