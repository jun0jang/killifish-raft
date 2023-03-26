package jraft.common.serialization

interface Deserializer<T> {

    fun configure(configs: Map<String, *>, isKey: Boolean) {
        // intentionally left blank
    }

    fun deserialize(topic: String, data: ByteArray)
}
