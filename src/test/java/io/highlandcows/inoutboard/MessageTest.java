package io.highlandcows.inoutboard;

import io.highlandcows.inoutboard.message.UserRegistrationMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Unit tests for {@link io.highlandcows.inoutboard.message.Message} and derived classes.
 *
 * @author highlandcows
 * @since 11/11/14
 */
public class MessageTest {

    @Test
    public void testUserRegistrationmessageEquals() throws Exception {
        UserRegistrationMessage msg1 = new UserRegistrationMessage("ABCD", "Test");
        UserRegistrationMessage msg2 = new UserRegistrationMessage("ABCD", "Test");
        assertEquals(msg1, msg2);
    }

    @Test
    public void testUserRegistrationMessageNotEquals() throws Exception {
        UserRegistrationMessage msg1 = new UserRegistrationMessage("ABCD", "Test");
        UserRegistrationMessage msg2 = new UserRegistrationMessage("WZYZ", "Test");
        assertNotEquals(msg1, msg2);
    }

    @Test
    public void testUserRegistrationMessageHashcodeEquals() throws Exception {
        UserRegistrationMessage msg1 = new UserRegistrationMessage("ABCD", "Test");
        UserRegistrationMessage msg2 = new UserRegistrationMessage("ABCD", "Test");
        assertEquals(msg1.hashCode(), msg2.hashCode());
    }

    @Test
    public void testUserRegistrationMessageHashcodeNotEquals() throws Exception {
        UserRegistrationMessage msg1 = new UserRegistrationMessage("ABCD", "Test");
        UserRegistrationMessage msg2 = new UserRegistrationMessage("WZYZ", "Test");
        assertNotEquals(msg1.hashCode(), msg2.hashCode());
    }

}
