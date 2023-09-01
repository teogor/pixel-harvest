package dev.teogor.pixel.harvest.database

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val discordId = long("discordId").uniqueIndex()
    val username = varchar("username", 100)

    override val primaryKey = PrimaryKey(id)
}