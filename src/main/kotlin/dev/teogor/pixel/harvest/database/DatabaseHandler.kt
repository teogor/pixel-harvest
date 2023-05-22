package dev.teogor.pixel.harvest.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseHandler(private val databasePath: String) {
    fun initializeDatabase() {
        // Connect to the SQLite database
        Database.connect("jdbc:sqlite:$databasePath", driver = "org.sqlite.JDBC")

        // Drop existing tables
        // transaction {
        //     SchemaUtils.drop(Users, Downloads)
        // }

        // Create the necessary tables
        transaction {
            SchemaUtils.create(Users, Downloads)
        }
    }

    fun addUser(discordId: Long, username: String) {
        transaction {
            val existingUser = getUser(discordId)
            if (existingUser == null) {
                Users.insert {
                    it[Users.discordId] = discordId
                    it[Users.username] = username
                }
            }
        }
    }

    fun addDownload(discordId: Long, url: String) {
        transaction {
            val existingUser = getUser(discordId)
            if (existingUser != null) {
                Downloads.insert {
                    it[Downloads.discordId] = existingUser.discordId
                    it[Downloads.url] = url
                    it[Downloads.timestamp] = System.currentTimeMillis() / 1000
                }
            }
        }
    }

    fun getTotalDownloadCountByDiscordUser(discordId: Long): Long {
        return transaction {
            Downloads.select { Downloads.discordId eq discordId }
                .count()
        }
    }

    private fun getUser(discordId: Long): User? {
        return transaction {
            val userRow = Users.select { Users.discordId eq discordId }.singleOrNull()
            userRow?.let {
                User(
                    id = it[Users.id],
                    discordId = it[Users.discordId],
                    username = it[Users.username]
                )
            }
        }
    }

    data class User(
        val id: Int,
        val discordId: Long,
        val username: String,
    )


}