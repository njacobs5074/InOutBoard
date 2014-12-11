package io.highlandcows.inoutboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.highlandcows.inoutboard.config.DatabaseConfig;
import io.highlandcows.inoutboard.config.InOutBoardServerTestConfig;
import io.highlandcows.inoutboard.config.UnregisterMessageHandlersConfig;
import io.highlandcows.inoutboard.config.WebSocketTestConfig;
import io.highlandcows.inoutboard.message.PongMessage;
import io.highlandcows.inoutboard.message.UserRegistrationMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.JsonPathExpectationsHelper;

import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test of {@link io.highlandcows.inoutboard.web.PingPongMessageController} using simulated STOMP messages.
 *
 * @author highlandcows
 * @since 10/12/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        InOutBoardServerTestConfig.class,
        DatabaseConfig.class,
        WebSocketTestConfig.class,
        UnregisterMessageHandlersConfig.class
})
@WebAppConfiguration
public class PingPongMessageControllerTest extends AbstractControllerTest {

    @Test
    public void testSinglePingMessage() throws Exception {

        // Start off by listening for pings.
        brokerChannelInterceptor.setIncludedDestinations("/topic/ping/**");
        brokerChannelInterceptor.startRecording();

        // Wait for a ping.
        org.springframework.messaging.Message<?> reply = brokerChannelInterceptor.awaitMessage(7);
        assertNotNull("Reply was null", reply);

        // Confirm that we received a valid ping message.
        StompHeaderAccessor stompHeaders = StompHeaderAccessor.wrap(reply);
        assertEquals("/topic/ping", stompHeaders.getDestination());

        readAndVerifyPingMessage(new String((byte[])reply.getPayload(), Charset.forName("UTF-8")));
    }

    @Test
    public void testPingPongMessage() throws Exception {

        // Register a user with the server.  This will initialize the 'last_updated' field in the
        // database.
        verifyRegisterSingleUser(TEST_HANDLE1, new UserRegistrationMessage(TEST_HANDLE1, TEST_NAME1));

        ResultSet rs = null;
        LocalDateTime lastUpdatedBefore, lastUpdatedAfter;

        // Fetch the last_updated timestamp directly from the database so that we can compare it later on.
        try {
            rs = embeddedDatabase.getConnection().createStatement().executeQuery(
                    "select last_updated from inout_board_user where user_handle = '" + TEST_HANDLE1 + "'");
            assertTrue("Expected record", rs.next());
            lastUpdatedBefore = rs.getTimestamp("last_updated").toLocalDateTime();
            assertNotNull("Expected last_updated to be non-null", lastUpdatedBefore);
        }
        finally {
            if (rs != null)
                rs.close();
        }

        // Wait a short interval and then send a response back to the server.
        Thread.sleep(100);
        PongMessage pongMessage = new PongMessage(TEST_HANDLE1, System.currentTimeMillis());
        StompHeaderAccessor stompHeaders = StompHeaderAccessor.create(StompCommand.SEND);
        stompHeaders.setSubscriptionId("0");
        stompHeaders.setDestination("/app/pong");
        stompHeaders.setSessionId("0");
        stompHeaders.setUser(new DummyPrincipal(TEST_HANDLE1));
        stompHeaders.setSessionAttributes(new HashMap<>(1));
        byte[] payload = new ObjectMapper().writeValueAsBytes(pongMessage);
        org.springframework.messaging.Message<byte[]> message = MessageBuilder.createMessage(payload, stompHeaders.getMessageHeaders());

        clientInboundChannel.send(message);
        Thread.sleep(100);

        // Check the database. The last_updated timestamp column should've been updated.
        try {
            rs = embeddedDatabase.getConnection().createStatement().executeQuery(
                    "select last_updated from inout_board_user where user_handle = '" + TEST_HANDLE1 + "'");
            assertTrue("Expected record", rs.next());
            lastUpdatedAfter = rs.getTimestamp("last_updated").toLocalDateTime();
            assertNotNull("Expected last_updated to be non-null", lastUpdatedAfter);
            assertTrue("Expected last_updated to have changed", lastUpdatedAfter.isAfter(lastUpdatedBefore));
        }
        finally {
            if (rs != null)
                rs.close();
        }
    }

    private void readAndVerifyPingMessage(String json) throws Exception {
        new JsonPathExpectationsHelper("$.type").assertValue(json, "PingMessage");

        // TODO - Figure out how to handle the $.time field
    }

}
