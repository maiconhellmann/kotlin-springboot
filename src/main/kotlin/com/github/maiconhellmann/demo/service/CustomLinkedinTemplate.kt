package com.github.maiconhellmann.demo.service

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.social.linkedin.api.impl.LinkedInTemplate
import org.springframework.social.support.HttpRequestDecorator
import org.springframework.util.Assert
import java.io.IOException
import java.net.URI

class CustomLinkedinTemplate(private val accessToken: String): LinkedInTemplate(accessToken) {


    init {
        Assert.hasLength(accessToken, "Access token cannot be null or empty.")
        registerOAuth2Interceptor(accessToken)
    }

    private fun registerOAuth2Interceptor(accessToken: String) {
        val interceptors = restTemplate.interceptors
        interceptors.add(OAuth2TokenParameterRequestInterceptor(accessToken))
        restTemplate.interceptors = interceptors
    }

    private class OAuth2TokenParameterRequestInterceptor(private val accessToken: String) : ClientHttpRequestInterceptor {

        @Throws(IOException::class)
        override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
            val protectedResourceRequest = object : HttpRequestDecorator(request) {
                override fun getURI(): URI {
                    var uri = super.getURI().toString()
                    uri = uri.replace("oauth2_access_token", "old_token")
                    return URI.create(uri)
                }
            }

            /**
             * Now linkedin accept oauth2 Bearer
             */
            protectedResourceRequest.headers.add("Authorization", "Bearer $accessToken")
            protectedResourceRequest.headers.add("x-li-src", "msdk")
            return execution.execute(protectedResourceRequest, body)
        }

    }
}