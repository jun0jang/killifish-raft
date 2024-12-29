package jraft.common.record

enum class TimestampType(val id: Int) {
    NO_TIMESTAMP_TYPE(-1),
    CREATE_TIME(0),
    LOG_APPEND_TIME(1),
}
