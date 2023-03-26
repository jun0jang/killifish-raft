package jraft.clients.consumer.internals

import jraft.common.serialization.Deserializer

class Deserializers<K, V>(
    val keyDeserializer: Deserializer<K>,
    val valueDeserializer: Deserializer<V>,
)
