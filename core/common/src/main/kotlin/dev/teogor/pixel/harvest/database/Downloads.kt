package dev.teogor.pixel.harvest.database

import org.jetbrains.exposed.sql.Table

object Downloads : Table() {
    val id = integer("id").autoIncrement()
    val discordId = reference("discordId", Users.discordId)
    val url = varchar("url", 2000)
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}
