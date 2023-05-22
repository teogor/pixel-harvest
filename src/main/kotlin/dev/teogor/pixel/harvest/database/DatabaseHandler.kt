package dev.teogor.pixel.harvest.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseHandler(private val databasePath: String) {
    fun initializeDatabase() {
        Database.connect("jdbc:sqlite:$databasePath", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(Users)
        }
    }

    // fun addUser(userId: Long, username: String, level: Int): Int {
    //     return transaction {
    //         Users.insertAndGetId {
    //             it[Users.userId] = userId
    //             it[Users.username] = username
    //             it[Users.level] = level
    //         }.value
    //     }
    // }

    fun getUsers(): List<User> {
        return transaction {
            Users.selectAll().map {
                User(
                    it[Users.id], it[Users.userId], it[Users.username], it[Users.level]
                )
            }
        }
    }

    data class User(
        val id: Int,
        val userId: Long,
        val username: String,
        val level: Int
    )
}