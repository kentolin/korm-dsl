package com.korm.examples.advanced.models

import com.korm.dsl.schema.Table

object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val username = varchar("username", 50).notNull().unique()
    val email = varchar("email", 100).notNull().unique()
    val age = int("age")
    val status = varchar("status", 20).notNull()
}

object Posts : Table("posts") {
    val id = int("id").primaryKey().autoIncrement()
    val userId = int("user_id").notNull().references(Users.id)
    val title = varchar("title", 200).notNull()
    val content = text("content").notNull()
    val likes = int("likes").notNull()
    val createdAt = varchar("created_at", 20).notNull()
}

object Comments : Table("comments") {
    val id = int("id").primaryKey().autoIncrement()
    val postId = int("post_id").notNull().references(Posts.id)
    val userId = int("user_id").notNull().references(Users.id)
    val content = text("content").notNull()
    val createdAt = varchar("created_at", 20).notNull()
}