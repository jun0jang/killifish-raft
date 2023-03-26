package jraft.common.requests

import jraft.common.protocol.ApiKeys

abstract class AbstractResponse(
    val apiKeys: ApiKeys,
)
