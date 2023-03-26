package jraft.clients.consumer

data class ConsumerRecord<K, V>(
    val key: K,
    val value: V,
)
