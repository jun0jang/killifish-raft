package jraft.common.requests

import jraft.common.protocol.ApiMessage

interface AbstractRequestResponse {
    fun data(): ApiMessage
}
