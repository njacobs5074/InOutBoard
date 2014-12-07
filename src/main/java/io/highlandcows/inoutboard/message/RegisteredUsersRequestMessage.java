package io.highlandcows.inoutboard.message;

/**
 * POJO that represents a request to get the current users.
 *
 * @author highlandcows
 * @since 12/11/14
 */
public class RegisteredUsersRequestMessage extends Message {

    public RegisteredUsersRequestMessage(String userHandle) {
        super(userHandle);
    }
}
