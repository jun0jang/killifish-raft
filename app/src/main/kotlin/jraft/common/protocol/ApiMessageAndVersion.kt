package jraft.common.protocol

data class ApiMessageAndVersion(
    val message: ApiMessage,
    val version: Short,
)
