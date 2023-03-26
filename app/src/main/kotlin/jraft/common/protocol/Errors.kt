package jraft.common.protocol

import jraft.common.errors.ApiException
import jraft.common.errors.OffsetOutOfRangeException
import jraft.common.errors.UnknownServerException

enum class Errors(
    val code: Short,
    val exception: ApiException?,
) {
    UNKNOWN_SERVER_ERROR(
        code = -1,
        exception = UnknownServerException("The server experienced an unexpected error when processing the request"),
    ),

    NONE(
        code = 0,
        exception = null,
    ),

    OFFSET_OUT_OF_RANGE(
        code = 1,
        exception = OffsetOutOfRangeException(),
    ),

    CORRUPT_MESSAGE(
        code = 2,
        exception = null,
    ),

    UNKNOWN_TOPIC_OR_PARTITION(
        code = 3,
        exception = null,
    ),
    ;

    companion object {
        private val CODE_TO_ERROR = values().associateBy { it.code }

        fun forCode(code: Short): Errors {
            return CODE_TO_ERROR[code] ?: UNKNOWN_SERVER_ERROR
        }
    }
}
