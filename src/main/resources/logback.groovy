import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%date{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

root(TRACE) {
    appenderRef("CONSOLE")

    // Exclude logs for Vectorizer AI
    logger("org.apache.hc.client5.http.wire") {
        level = ERROR
        additivity = false
    }

    // Exclude logs for Database Queries
    logger("org.jetbrains.exposed.sql") {
        level = ERROR
        additivity = false
    }

    // Exclude logs that don't have an error level and are made by the DEBUG [R]:[KTOR]:[ExclusionRequestRateLimiter] logger
    logger("[R]:[KTOR]:[ExclusionRequestRateLimiter]") {
        level = OFF
        additivity = false
    }
}
