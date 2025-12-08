// korm-dsl/korm-dsl-validation/src/test/kotlin/com/korm/dsl/validation/validators/StringValidatorsTest.kt

package com.korm.dsl.validation.validators

import com.korm.dsl.validation.validator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

data class StringData(val value: String?)

class StringValidatorsTest : StringSpec({

    "email validator should accept valid emails" {
        val validator = validator<StringData> {
            field(StringData::value) {
                email()
            }
        }

        validator.isValid(StringData("test@example.com")) shouldBe true
        validator.isValid(StringData("user.name+tag@example.co.uk")) shouldBe true
    }

    "email validator should reject invalid emails" {
        val validator = validator<StringData> {
            field(StringData::value) {
                email()
            }
        }

        validator.isValid(StringData("invalid")) shouldBe false
        validator.isValid(StringData("@example.com")) shouldBe false
        validator.isValid(StringData("user@")) shouldBe false
    }

    "pattern validator should match regex" {
        val validator = validator<StringData> {
            field(StringData::value) {
                pattern("^[A-Z]{3}$")
            }
        }

        validator.isValid(StringData("ABC")) shouldBe true
        validator.isValid(StringData("abc")) shouldBe false
        validator.isValid(StringData("ABCD")) shouldBe false
    }

    "UUID validator should accept valid UUIDs" {
        val validator = validator<StringData> {
            field(StringData::value) {
                uuid()
            }
        }

        validator.isValid(StringData("550e8400-e29b-41d4-a716-446655440000")) shouldBe true
        validator.isValid(StringData("invalid-uuid")) shouldBe false
    }
})
