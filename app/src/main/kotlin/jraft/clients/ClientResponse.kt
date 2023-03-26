package jraft.clients

import jraft.common.errors.AuthenticationException
import jraft.common.errors.UnsupportedVersionException
import jraft.common.requests.AbstractResponse
import jraft.common.requests.RequestHeader

class ClientResponse(
    val requestHeader: RequestHeader,
    val callback: RequestCompletionHandler?,
    val destination: String,
    val receivedTimeMs: Long,
    val latencyMs: Long,
    val disconnected: Boolean,
    private val timeout: Boolean,
    val versionMismatch: UnsupportedVersionException?,
    val authenticationException: AuthenticationException?,
    val responseBody: AbstractResponse?,
) {
    companion object {
        fun create(
            requestHeader: RequestHeader,
            callback: RequestCompletionHandler?,
            destination: String,
            createdTimeMs: Long,
            receivedTimeMs: Long,
            disconnected: Boolean,
            timeout: Boolean,
            versionMismatch: UnsupportedVersionException?,
            authenticationException: AuthenticationException?,
            responseBody: AbstractResponse?,
        ): ClientResponse {
            return ClientResponse(
                requestHeader = requestHeader,
                callback = callback,
                destination = destination,
                receivedTimeMs = receivedTimeMs,
                latencyMs = receivedTimeMs - createdTimeMs,
                disconnected = disconnected,
                timeout = timeout,
                versionMismatch = versionMismatch,
                authenticationException = authenticationException,
                responseBody = responseBody,
            )
        }
    }

    fun wasTimeout(): Boolean {
        return timeout
    }

    fun wasDisconnected(): Boolean {
        return disconnected
    }

    fun hasResponse(): Boolean {
        return responseBody != null
    }

    fun onComplete() {
        callback?.onComplete(this)
    }
}
