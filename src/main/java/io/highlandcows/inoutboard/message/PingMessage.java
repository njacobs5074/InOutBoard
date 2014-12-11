package io.highlandcows.inoutboard.message;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Message sent by server to clients to determine if they're still there.
 *
 * @author highlandcows
 * @since 10/12/14
 */
public class PingMessage {
    protected final String type = getClass().getSimpleName();
    private LocalDateTime localDateTime;

    public PingMessage() {
        localDateTime = LocalDateTime.now();
    }

    @JsonIgnore
    public LocalDateTime getDateTime() {
        return localDateTime;
    }

    public String getType() { return type; }

    public long getTime() {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void setTime(long millis) {
        localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        return localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
