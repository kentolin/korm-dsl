// korm-dsl/korm-dsl-validation/src/main/kotlin/com/korm/dsl/validation/groups/ValidationGroups.kt

package com.korm.dsl.validation.groups

/**
 * Validation groups for conditional validation.
 */
interface ValidationGroup

/**
 * Default validation group.
 */
interface Default : ValidationGroup

/**
 * Validation group for create operations.
 */
interface Create : ValidationGroup

/**
 * Validation group for update operations.
 */
interface Update : ValidationGroup

/**
 * Validation group for delete operations.
 */
interface Delete : ValidationGroup

/**
 * Validation group for complete validation.
 */
interface Complete : ValidationGroup

/**
 * Validation group for partial validation.
 */
interface Partial : ValidationGroup
