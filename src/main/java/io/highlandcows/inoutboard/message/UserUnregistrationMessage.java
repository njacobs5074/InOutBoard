package io.highlandcows.inoutboard.message;

/**
 * POJO that represents a disconnect from the server.
 *
 * @author highlandcows
 * @since 12/11/14
 */
public class UserUnregistrationMessage extends Message {
    public UserUnregistrationMessage() {}

    public UserUnregistrationMessage(String userHandle) {
        super(userHandle);
    }
}
