package io.highlandcows.inoutboard.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * POJO to represent an in/out board user.
 *
 * @author highlandcows
 * @since 11/11/14
 */
public class InOutBoardUser {
    private String handle;
    private String name;
    private InOutBoardStatus status;
    private String comment;

    public InOutBoardUser(String handle, String name, InOutBoardStatus status, String comment) {
        this.handle = handle;
        this.name = name;
        this.status = status;
        this.comment = StringUtils.isBlank(comment) ? "" : comment;
    }

    public InOutBoardUser(String handle, String name) {
        this(handle, name, InOutBoardStatus.UNKNOWN, "");
    }

    public InOutBoardUser(String handle) {
        this(handle, "");
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InOutBoardStatus getStatus() {
        return status;
    }

    public void setStatus(InOutBoardStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public int hashCode() {
        return handle.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return handle.equals(((InOutBoardUser)o).handle);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("handle", handle)
                .append("name", name)
                .append("status", status)
                .append("comment", comment).toString();
    }

    public static boolean isValid(InOutBoardUser inOutBoardUser) {
        return inOutBoardUser != null &&
               StringUtils.isNotBlank(inOutBoardUser.getHandle()) &&
               StringUtils.isNotBlank(inOutBoardUser.getName());
    }
}
