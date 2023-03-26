package jraft.clients.consumer.internals

import java.util.function.Supplier

class CachedSupplier<T>(
    private val create: () -> T,
) : Supplier<T> {
    private var result: T? = null

    override fun get(): T {
        if (result == null) {
            result = create()
        }
        return result!!
    }
}
