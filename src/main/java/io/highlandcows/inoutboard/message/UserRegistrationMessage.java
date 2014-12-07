package io.highlandcows.inoutboard.message;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * POJO that represents a user registration message.
 *
 * @author highlandcows
 * @since 10/11/14
 */
public class UserRegistrationMessage extends Message {
    protected String name;

    /**
     * Required by Jackson JSON framework.
     */
    public UserRegistrationMessage() {}

    /**
     * Create a user registration message with the required parameters.
     * @param userHandle - User's shorthand name
     * @param name - User's name
     */
    public UserRegistrationMessage(String userHandle, String name) {
        super(userHandle);
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(19, 41).appendSuper(super.hashCode()).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != getClass()) return false;

        UserRegistrationMessage that = (UserRegistrationMessage)o;
        return new EqualsBuilder()
                .appendSuper(super.equals(that))
                .append(name, that.name)
                .isEquals();
    }
}
