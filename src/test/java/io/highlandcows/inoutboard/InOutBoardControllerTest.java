package io.highlandcows.inoutboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.highlandcows.inoutboard.config.DatabaseConfig;
import io.highlandcows.inoutboard.config.InOutBoardServerTestConfig;
import io.highlandcows.inoutboard.config.UnregisterMessageHandlersConfig;
import io.highlandcows.inoutboard.config.WebSocketTestConfig;
import io.highlandcows.inoutboard.message.UserRegistrationMessage;
import io.highlandcows.inoutboard.message.UserStatusUpdateMessage;
import io.highlandcows.inoutboard.model.InOutBoardStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test the functionality of the {@link io.highlandcows.inoutboard.web.InOutBoardController} using
 * simulated STOMP & REST message exchanges.  Based on code in
 * {@see https://github.com/rstoyanchev/spring-websocket-portfolio/tree/master/src/test/java/org/springframework/samples/portfolio/web}
 *
 * @author highlandcows
 * @since 14/11/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        InOutBoardServerTestConfig.class,
        DatabaseConfig.class,
        WebSocketTestConfig.class,
        UnregisterMessageHandlersConfig.class
})
@WebAppConfiguration
public class InOutBoardControllerTest extends AbstractControllerTest {

    /**
     * Test retrieving all of the status values.  Used by client to initialize UI.
     *
     * @throws Exception
     */
    @Test
    public void testGetUserStatusValues() throws Exception {

        // If you consult InOutBoardStatus, you'll see that we use #getDescription() as the JSON representation.
        // Here, we create a String[] that has those values.  Lambdas make this a relatively compact operation.
        // Note that we also filter out the so-called 'system' status values because the web service does not
        // return them.
        String[] values = Arrays.asList(InOutBoardStatus.values()).stream()
                                .filter(status -> !status.isSystemStatus())
                                .map(InOutBoardStatus::getDescription)
                                .toArray(String[]::new);

        // Now we use the MockMvc to invoke the REST interface and then check that the content returned
        // is the JSON we expect.
        mockMvc.perform(get("/inoutboard-rest/user-status-values/"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(contentType))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(values.length)))
               .andExpect(jsonPath("@", containsInAnyOrder(values)));
    }

    /**
     * Test registering a user and the asynchronous status update associated with it.
     *
     * @throws Exception
     */
    @Test
    public void testRegisterUser() throws Exception {

        // Start off by listening for user updates.
        brokerChannelInterceptor.setIncludedDestinations("/topic/**");
        brokerChannelInterceptor.startRecording();

        // Register a user and confirm that it was registered.
        verifyRegisterSingleUser(TEST_HANDLE1, new UserRegistrationMessage(TEST_HANDLE1, TEST_NAME1));

        // Check to see if we got a user status update as a result.
        org.springframework.messaging.Message<?> reply = brokerChannelInterceptor.awaitMessage(5);
        assertNotNull("Reply was null", reply);

        // The message should simply indicate that the user is now registered.
        StompHeaderAccessor userRegistrationHeaders = StompHeaderAccessor.wrap(reply);
        assertEquals("/topic/user-status-update", userRegistrationHeaders.getDestination());
        readAndVerifyUserStatusUpdateMessage(new String((byte[])reply.getPayload(), Charset.forName("UTF-8")),
                                             TEST_HANDLE1, InOutBoardStatus.REGISTERED.getDescription(),
                                             "");
    }

    /**
     * Test unregistering a user and the asynchronous status update associated with it.
     *
     * @throws Exception
     */
    @Test
    public void testUnregisterUser() throws Exception {

        // Start off by listening for user updates.
        brokerChannelInterceptor.setIncludedDestinations("/topic/**");
        brokerChannelInterceptor.startRecording();

        verifyRegisterSingleUser(TEST_HANDLE1, new UserRegistrationMessage(TEST_HANDLE1, TEST_NAME1));

        // Check to see if we got a user status update as a result.
        org.springframework.messaging.Message<?> reply = brokerChannelInterceptor.awaitMessage(5);
        assertNotNull("Reply was null", reply);

        // The message should simply indicate that the user is now registered.
        StompHeaderAccessor userRegistrationHeaders = StompHeaderAccessor.wrap(reply);
        assertEquals("/topic/user-status-update", userRegistrationHeaders.getDestination());
        readAndVerifyUserStatusUpdateMessage(new String((byte[])reply.getPayload(), Charset.forName("UTF-8")),
                                             TEST_HANDLE1, InOutBoardStatus.REGISTERED.getDescription(),
                                             "");
       verifyUnregisterSingleUser(TEST_HANDLE1);

        // Check to see if we got a user status update as a result.
        reply = brokerChannelInterceptor.awaitMessage(5);
        assertNotNull("Reply was null", reply);

        // The message should simply indicate that the user is now registered.
        userRegistrationHeaders = StompHeaderAccessor.wrap(reply);
        assertEquals("/topic/user-status-update", userRegistrationHeaders.getDestination());
        readAndVerifyUserStatusUpdateMessage(new String((byte[])reply.getPayload(), Charset.forName("UTF-8")),
                                             TEST_HANDLE1, InOutBoardStatus.UNREGISTERED.getDescription(),
                                             "");
    }

    /**
     * Test getting the current users and their status.  Here, we use the bulk HTTP interface as opposed to the STOMP
     * messaging one.
     *
     * @throws Exception
     */
    @Test
    public void testUserStatusRefresh() throws Exception {

        // Register 2 users.
        verifyRegisterSingleUser(TEST_HANDLE1, new UserRegistrationMessage(TEST_HANDLE1, TEST_NAME1));
        verifyRegisterSingleUser(TEST_HANDLE2, new UserRegistrationMessage(TEST_HANDLE2, TEST_NAME2));

        mockMvc.perform(get("/inoutboard-rest/get-all-users"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(contentType))
               .andExpect(jsonPath("$").isArray())
               .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testUserStatusUpdate() throws Exception {

        // Start off by listening for user updates.
        brokerChannelInterceptor.setIncludedDestinations("/topic/**");
        brokerChannelInterceptor.startRecording();

        verifyRegisterSingleUser(TEST_HANDLE1, new UserRegistrationMessage(TEST_HANDLE1, TEST_NAME1));

        // Check to see if we got a user status update as a result.
        org.springframework.messaging.Message<?> reply = brokerChannelInterceptor.awaitMessage(5);
        assertNotNull("Reply was null", reply);

        // The message should simply indicate that the user is now registered.
        StompHeaderAccessor stompHeaders = StompHeaderAccessor.wrap(reply);
        assertEquals("/topic/user-status-update", stompHeaders.getDestination());
        readAndVerifyUserStatusUpdateMessage(new String((byte[])reply.getPayload(), Charset.forName("UTF-8")),
                                             TEST_HANDLE1, InOutBoardStatus.REGISTERED.getDescription(), "");

        UserStatusUpdateMessage newStatus = new UserStatusUpdateMessage(TEST_HANDLE1, TEST_HANDLE1, InOutBoardStatus.AVAILABLE, "At my desk",
                                                                        LocalDateTime.now());

        String payload = new String(new ObjectMapper().writeValueAsBytes(newStatus));
        mockMvc.perform(post("/inoutboard-rest/user-status-update").contentType(contentType).content(payload)).andExpect(status().isOk());
    }
}
