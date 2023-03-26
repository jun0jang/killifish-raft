package jraft.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T> logger(clazz: Class<*>): Logger {
    return LoggerFactory.getLogger(clazz)
}

inline fun <reified T> logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
