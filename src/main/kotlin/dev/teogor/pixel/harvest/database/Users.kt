package dev.teogor.pixel.harvest.database

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = integer("id").autoIncrement()
    val userId = long("userId").uniqueIndex()
    val username = varchar("username", 100)
    val discriminator = varchar("discriminator", 4)
    val level = integer("level")

    override val primaryKey = PrimaryKey(id)
}