package jraft.clients.consumer

import jraft.common.Configurable

interface ConsumerInterceptor<K, V> : Configurable {

    fun onConsume(records: ConsumerRecords<K, V>): ConsumerRecords<K, V>

    fun close()
}
