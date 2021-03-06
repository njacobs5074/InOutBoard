package io.highlandcows.inoutboard.message;

import io.highlandcows.inoutboard.model.InOutBoardStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author highlandcows
 * @since 10/11/14
 */
public class UserStatusUpdateMessage extends Message {
    private InOutBoardStatus inOutBoardStatus;

    private String name;

    private String comment;

    private LocalDateTime lastUpdated;

    public UserStatusUpdateMessage() {}

    public UserStatusUpdateMessage(String userHandle, String name, InOutBoardStatus inOutBoardStatus, String comment, LocalDateTime lastUpdated) {
        super(userHandle);
        this.name = name;
        this.inOutBoardStatus = inOutBoardStatus;
        this.comment = StringUtils.isBlank(comment) ? "" : comment;
        this.lastUpdated = lastUpdated;
    }


    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public InOutBoardStatus getInOutBoardStatus() { return inOutBoardStatus; }

    public void setInOutBoardStatus(InOutBoardStatus inOutBoardStatus) { this.inOutBoardStatus = inOutBoardStatus; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public long getLastUpdated() {
        return lastUpdated.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void setLastUpdated(long millis) {
        this.lastUpdated = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 31)
                .appendSuper(super.hashCode())
                .append(name)
                .append(inOutBoardStatus)
                .append(comment)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;

        UserStatusUpdateMessage that = (UserStatusUpdateMessage)o;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(name, that.name)
                .append(inOutBoardStatus, that.inOutBoardStatus)
                .append(comment, that.comment)
                .isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("handle", handle)
                .append("name", name)
                .append("status", inOutBoardStatus)
                .append("comment", comment).toString();
    }
}
