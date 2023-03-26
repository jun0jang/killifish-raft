package jraft.console

import jraft.clients.consumer.Consumer
import jraft.clients.consumer.OffsetResetStrategy
import jraft.clients.consumer.SubscriptionState
import jraft.clients.consumer.internals.FetchCollector
import jraft.clients.consumer.internals.evetns.ApplicationEvent
import jraft.clients.consumer.internals.evetns.ApplicationEventHandler
import jraft.clients.consumer.internals.evetns.ApplicationEventProcessor
import jraft.clients.consumer.internals.metrics.KafkaConsumerMetrics
import jraft.common.metrics.Metrics
import jraft.common.utils.Time
import java.util.concurrent.LinkedBlockingQueue

class ConsoleConsumer {

    fun run() {
        val time = Time.SYSTEM
        val subscription = SubscriptionState(defaultResetStrategy = OffsetResetStrategy.LATEST)

        val applicationEventQueue = LinkedBlockingQueue<ApplicationEvent>()
        val applicationEventProcessorSupplier = ApplicationEventProcessor.supplier(
            queue = applicationEventQueue,
        )
        val metrics = Metrics()

        val consumer = Consumer<String, String>(
            time = time,
            subscription = subscription,
            fetchCollector = FetchCollector(subscription, TODO()),
            applicationEventHandler = ApplicationEventHandler(
                time = time,
                applicationEventProcessorSupplier = applicationEventProcessorSupplier,
                applicationEventQueue = applicationEventQueue,
            ),
            kafkaConsumerMetrics = KafkaConsumerMetrics.create(metrics, "console-consumer"),
        )

        consumer.commitAsync()
        consumer.commitAsync()
    }
}

fun main() {
    ConsoleConsumer().run()
}
