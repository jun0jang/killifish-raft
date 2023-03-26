package jraft.clients.consumer.internals

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import jraft.clients.consumer.internals.metrics.KafkaConsumerMetrics
import jraft.common.metrics.Metrics

class KafkaConsumerMetricsTest : FreeSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val time = FakeTime()

    val metrics = Metrics(time = time)

    val consumerMetrics = KafkaConsumerMetrics.create(metrics, "consumer")

    /**
     * cc KafkaConsumerTest.testPollIdleRatio
     * poll idle ratio의 의미
     * - x: poll 함수의 실행 시간 == time-inside-poll
     * - y: poll이 다시 호출 되기까지 걸린 시간 == record processing time
     * - poll idle ratio = x / (x + y)
     * - poll idel ratio = time-inside-poll / total-time
     * - 전체 시간에서 poll 함수가 차지하는 비율.
     *
     * poll이 0ms안에 계속 호출된다면 poll idle ratio는 1이 된다.
     * poll을 다시 호출하는데 무한에 가까운 시간이 걸린다면 poll idle ratio는 0에 수렴한다.
     */
    "testPollIdleRatio" {
        val pollIdleRatio = metrics.metricName("poll-idle-ratio-avg", "consumer-metrics")

        // 1st poll
        consumerMetrics.recordPollStart(time.milliseconds())
        time.sleep(50)
        consumerMetrics.recordPollEnd(time.milliseconds())

        // poll 함수의 실행 시간: 50ms
        // poll이 다시 호출 되기까지 걸린 시간: 0ms
        metrics.metrics[pollIdleRatio]!!.metricValue() shouldBe 1.0

        // 2nd poll
        time.sleep(50)
        consumerMetrics.recordPollStart(time.milliseconds()) // timeSinceLastPollMs: 100ms
        consumerMetrics.recordPollEnd(time.milliseconds())

        // poll 함수의 실행 시간: 0ms
        // poll이 다시 호출 되기까지 걸린 시간: 100ms
        metrics.metrics[pollIdleRatio]!!.metricValue() shouldBe (1.0 + 0.0) / 2

        // 3rd poll
        time.sleep(25)
        consumerMetrics.recordPollStart(time.milliseconds())
        time.sleep(25)
        consumerMetrics.recordPollEnd(time.milliseconds())

        // poll 함수의 실행 시간: 25ms
        // poll이 다시 호출 되기까지 걸린 시간: 25ms
        metrics.metrics[pollIdleRatio]!!.metricValue() shouldBe (1.0 + 0.0 + 0.5) / 3
    }
})
