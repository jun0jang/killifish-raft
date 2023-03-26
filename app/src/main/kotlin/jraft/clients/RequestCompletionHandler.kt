package jraft.clients

interface RequestCompletionHandler {
    fun onComplete(response: ClientResponse)
}
