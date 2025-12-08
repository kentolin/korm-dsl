// korm-dsl/korm-dsl-monitoring/src/test/kotlin/com/korm/dsl/monitoring/MetricsTest.kt

package com.korm.dsl.monitoring

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class MetricsTest : FunSpec({

    test("counter should increment") {
        val counter = Counter("test.counter")

        counter.increment()
        counter.increment(5)

        counter.count() shouldBe 6
    }

    test("gauge should track value") {
        val gauge = SimpleGauge("test.gauge")

        gauge.set(42.0)
        gauge.value() shouldBe 42.0

        gauge.increment(8.0)
        gauge.value() shouldBe 50.0
    }

    test("timer should track durations") {
        val timer = Timer("test.timer")

        val result = timer.record {
            Thread.sleep(10)
            "done"
        }

        result shouldBe "done"
        timer.count() shouldBe 1
        timer.mean() shouldNotBe 0.0
    }

    test("histogram should track distribution") {
        val histogram = Histogram("test.histogram")

        histogram.record(10)
        histogram.record(20)
        histogram.record(30)

        histogram.count() shouldBe 3
        histogram.mean() shouldBe 20.0
        histogram.min() shouldBe 10
        histogram.max() shouldBe 30
    }
})
