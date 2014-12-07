package io.highlandcows.inoutboard.message;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * @author highlandcows
 * @since 10/11/14
 */
public abstract class Message {
    protected final String type = getClass().getSimpleName();
    protected String handle;

    protected Message() {}

    protected Message(String handle) {
        this.handle = handle;
    }

    public String getType() { return type; }
    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        return new EqualsBuilder().append(handle, ((Message)o).handle).isEquals();
    }

    @Override
    public int hashCode() {
        return handle == null ? 0 : handle.hashCode();
    }
}
