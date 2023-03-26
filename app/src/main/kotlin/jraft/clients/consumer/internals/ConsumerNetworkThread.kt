package jraft.clients.consumer.internals

import jraft.clients.consumer.internals.evetns.ApplicationEventProcessor
import jraft.common.IdempotentCloser
import jraft.common.logger
import jraft.common.utils.Time
import java.time.Duration
import java.util.function.Supplier

class ConsumerNetworkThread(
    val time: Time,
    private val applicationEventProcessorSupplier: Supplier<ApplicationEventProcessor>,
    private val networkClientDelegateSupplier: Supplier<NetworkClientDelegate>,
    private val closer: IdempotentCloser = IdempotentCloser(),
    private var closeTimeout: Duration = Duration.ofSeconds(30),
    private val requestManagers: RequestManagers,
) : Thread(), AutoCloseable {
    private val log = logger<ConsumerNetworkThread>()

    private lateinit var applicationEventProcessor: ApplicationEventProcessor

    private lateinit var networkClientDelegate: NetworkClientDelegate

    @Volatile
    private var isRunning: Boolean = true

    override fun run() {
        initializeResources()

        while (isRunning) {
            try {
                runOnce()
            } catch (e: Throwable) {
                println(e)
            }
        }
    }

    private fun runOnce() {
        applicationEventProcessor.process()

        val currentTimeMs = time.milliseconds()
        var pollWaitTimeMs = MAX_POLL_TIMEOUT_MS
        for (requestManager in requestManagers.entries()) {
            val pollResult = requestManager.poll(currentTimeMs)
            pollWaitTimeMs = minOf(pollWaitTimeMs, pollResult.timeUntilNextPollMs)
            networkClientDelegate.addAll(pollResult)
        }
        networkClientDelegate.poll(pollWaitTimeMs, currentTimeMs)
    }

    private fun initializeResources() {
        applicationEventProcessor = this.applicationEventProcessorSupplier.get()

        networkClientDelegate = this.networkClientDelegateSupplier.get()
    }

    fun wakeup() {
    }

    override fun close() {
        closer.close(
            onInitialClose = {
                isRunning = false
                wakeup()

                join()
            },
            onSubsequentClose = {
                log.warn("The consumer network thread was already closed")
            },
        )
    }

    fun maximumTimeToWait(): Long {
        return MAX_POLL_TIMEOUT_MS
    }

    private fun closeInternal(timeout: Duration) {
        isRunning = false
        closeTimeout = timeout
        wakeup()

        try {
            join()
        } catch (e: InterruptedException) {
            log.warn("Interrupted while waiting for the consumer network thread to close", e)
        }
    }

    companion object {
        val MAX_POLL_TIMEOUT_MS = 5000L
    }
}
