package jraft.common.config

import java.util.concurrent.ConcurrentHashMap

abstract class AbstractConfig {
    private val originals = mutableMapOf<String, Any>()

    private val values = mutableMapOf<String, Any>()

    private val used = ConcurrentHashMap.newKeySet<String>()

    fun <T> get(key: String): T {
        if (!values.containsKey(key)) {
            throw IllegalArgumentException("No value found for key $key")
        }
        used.add(key)
        return values[key] as T
    }

    fun unused(): Set<String> {
        return values.keys - used
    }

    fun originalsWithPrefix(
        prefix: String,
        strip: Boolean = true,
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        for (entry in originals.entries) {
            if (entry.key.startsWith(prefix) && entry.key.length > prefix.length) {
                val key = if (strip) entry.key.substring(prefix.length) else entry.key
                result[key] = entry.value
            }
        }

        return values.filterKeys { it.startsWith(prefix) }
    }
}
