// korm-dsl/korm-dsl-validation/src/test/kotlin/com/korm/dsl/validation/EntityValidatorTest.kt

package com.korm.dsl.validation

import com.korm.dsl.validation.validators.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val age: Int,
    val phone: String?
)

class EntityValidatorTest : FunSpec({

    val userValidator = validator<User> {
        field(User::name) {
            notBlank()
            minLength(3)
            maxLength(50)
        }

        field(User::email) {
            notBlank()
            email()
        }

        field(User::age) {
            positive()
            range(18, 120)
        }

        field(User::phone) {
            phone()
        }
    }

    test("should validate valid user") {
        val user = User(
            name = "John Doe",
            email = "john@example.com",
            age = 25,
            phone = "+1234567890"
        )

        val result = userValidator.validate(user)
        result.isValid shouldBe true
    }

    test("should detect invalid email") {
        val user = User(
            name = "John Doe",
            email = "invalid-email",
            age = 25,
            phone = null
        )

        val result = userValidator.validate(user)
        result.isValid shouldBe false
        result.violations.any { it.field == "email" } shouldBe true
    }

    test("should detect age out of range") {
        val user = User(
            name = "John Doe",
            email = "john@example.com",
            age = 15,
            phone = null
        )

        val result = userValidator.validate(user)
        result.isValid shouldBe false
        result.violations.any { it.field == "age" } shouldBe true
    }

    test("should detect short name") {
        val user = User(
            name = "Jo",
            email = "john@example.com",
            age = 25,
            phone = null
        )

        val result = userValidator.validate(user)
        result.isValid shouldBe false
        result.violations.any { it.field == "name" } shouldBe true
    }
})
