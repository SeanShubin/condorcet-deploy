package com.seanshubin.condorcet.deploy.domain

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HttpImpl(val httpClient: HttpClient) : Http {
  override fun getString(uriString: String): String {
    val uri = URI.create(uriString)
    val request = HttpRequest.newBuilder().GET().uri(uri).build()
    val responseBodyHandler = HttpResponse.BodyHandlers.ofString()
    val responseBody = httpClient.send(request, responseBodyHandler)
    val body = responseBody.body()
    return body
  }
}
