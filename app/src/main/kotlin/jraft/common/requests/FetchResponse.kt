package jraft.common.requests

import jraft.common.protocol.ApiKeys

class FetchResponse(
    apiKeys: ApiKeys,
) : AbstractResponse(apiKeys = apiKeys)
