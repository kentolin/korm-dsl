// korm-dsl/korm-dsl-monitoring/src/test/kotlin/com/korm/dsl/monitoring/HealthCheckTest.kt

package com.korm.dsl.monitoring

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class HealthCheckTest : StringSpec({

    "health check should return UP for healthy system" {
        val healthCheck = object : HealthCheck {
            override val name = "test"
            override fun check() = HealthCheckResult(HealthStatus.UP)
        }

        val result = healthCheck.check()
        result.status shouldBe HealthStatus.UP
        result.isHealthy() shouldBe true
    }

    "composite health check should aggregate results" {
        val check1 = object : HealthCheck {
            override val name = "service1"
            override fun check() = HealthCheckResult(HealthStatus.UP)
        }

        val check2 = object : HealthCheck {
            override val name = "service2"
            override fun check() = HealthCheckResult(HealthStatus.DOWN, error = "Service unavailable")
        }

        val composite = CompositeHealthCheck(listOf(check1, check2))
        val result = composite.check()

        result.status shouldBe HealthStatus.DOWN
    }
})
