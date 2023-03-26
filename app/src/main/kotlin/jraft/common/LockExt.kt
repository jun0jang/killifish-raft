package jraft.common

import java.util.concurrent.locks.Lock

fun <T>Lock.execute(action: () -> T): T {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}
