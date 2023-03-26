package jraft.clients.consumer.internals

import jraft.clients.consumer.ConsumerInterceptor
import jraft.clients.consumer.ConsumerRecords

class ConsumerInterceptors<K, V>(
    private val interceptors: List<ConsumerInterceptor<K, V>>,
) {

    fun onConsume(records: ConsumerRecords<K, V>): ConsumerRecords<K, V> {
        var interceptRecords = records
        for (interceptor in interceptors) {
            try {
                interceptRecords = interceptor.onConsume(records)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return interceptRecords
    }
}
