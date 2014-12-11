package io.highlandcows.inoutboard.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Response to {@link io.highlandcows.inoutboard.message.PingMessage}.  Note that
 * this message contains the user's handle so that we can tie it back to the
 * persistent store.
 *
 * @author highlandcows
 * @since 10/12/14
 */
public class PongMessage extends Message {

    private long millis;

    /**
     * Used by Jackson JSON framework.
     */
    public PongMessage() {}

    public PongMessage(String handle, long millis) {
        super(handle);
        this.millis = millis;
    }

    @JsonIgnore
    public LocalDateTime getDateTime() {
        return LocalDateTime.ofEpochSecond(millis, 0, ZoneOffset.UTC);
    }

    public long getTime() {
        return millis;
    }

    public void setTime(long millis) { this.millis = millis; }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("handle", handle)
                .append("datetime", getDateTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .toString();
    }
}
