package jraft.clients.consumer.internals

import java.util.function.Supplier

class RequestManagers(
    private val fetchRequestManager: FetchRequestManager,
) {
    fun entries(): List<RequestManager> {
        return listOf(fetchRequestManager)
    }

    companion object {
        fun supplier(
            fetchRequestManager: FetchRequestManager,
        ): Supplier<RequestManagers> {
            return CachedSupplier {
                RequestManagers(
                    fetchRequestManager = fetchRequestManager,
                )
            }
        }
    }
}
