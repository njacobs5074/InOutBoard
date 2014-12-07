package io.highlandcows.inoutboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.highlandcows.inoutboard.config.DatabaseConfig;
import io.highlandcows.inoutboard.config.InOutBoardServerTestConfig;
import io.highlandcows.inoutboard.config.UnregisterMessageHandlersConfig;
import io.highlandcows.inoutboard.config.WebSocketTestConfig;
import io.highlandcows.inoutboard.message.UserRegistrationMessage;
import io.highlandcows.inoutboard.message.UserStatusUpdateMessage;
import io.highlandcows.inoutboard.message.UserUnregistrationMessage;
import io.highlandcows.inoutboard.model.InOutBoardStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

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
public class InOutBoardControllerTest {

    private static final String TEST_HANDLE1 = "njacobs5074";
    private static final String TEST_NAME1 = "Nick Jacobs";
    private static final String TEST_HANDLE2 = "THX1138";
    private static final String TEST_NAME2 = "George Lucas";

    @Autowired
    private AbstractSubscribableChannel clientInboundChannel;

    @Autowired
    private AbstractSubscribableChannel brokerChannel;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    EmbeddedDatabase embeddedDatabase;

    private MediaType contentType;

    private MockMvc mockMvc;

    private TestChannelInterceptor brokerChannelInterceptor;

    // Check to make sure that our test context has a JSON converter provided
    // as part of the Spring MVC environment.
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    void checkForJSONConverter(HttpMessageConverter<?>[] converters) {
        assertNotNull("JSON message converter is null",
                      Arrays.asList(converters).stream()
                            .filter(httpMessageConverter -> httpMessageConverter instanceof MappingJackson2HttpMessageConverter)
                            .findAny().get());
    }

    @Before
    public void setUp() throws Exception {
        mockMvc = webAppContextSetup(webApplicationContext).build();

        brokerChannelInterceptor = new TestChannelInterceptor(false);
        brokerChannel.addInterceptor(brokerChannelInterceptor);

        contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
                                    MediaType.APPLICATION_JSON.getSubtype(),
                                    Charset.forName("utf8"));
    }

    @After
    public void cleanUp() throws Exception {
        embeddedDatabase.getConnection().createStatement().execute("delete from inout_board_user");
        brokerChannelInterceptor.stopRecording();
        brokerChannelInterceptor.setIncludedDestinations("");
    }

    /**
     * Test retrieving all of the status values.  Used by client to initialize UI.
     *
     * @throws Exception
     */
    @Test
    public void testGetUserStatusValues() throws Exception {

        // If you consult InOutBoardStatus, you'll see that we use #getDescription() as the JSON representation.
        // Here, we create a String[] that has those values.  Lambdas make this a relatively compact operation.
        String[] values = Arrays.asList(InOutBoardStatus.values()).stream()
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
                                             TEST_HANDLE1, TEST_NAME1, InOutBoardStatus.REGISTERED.getDescription(),
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
                                             TEST_HANDLE1, TEST_NAME1, InOutBoardStatus.REGISTERED.getDescription(),
                                             "");
       verifyUnregisterSingleUser(TEST_HANDLE1);

        // Check to see if we got a user status update as a result.
        reply = brokerChannelInterceptor.awaitMessage(5);
        assertNotNull("Reply was null", reply);

        // The message should simply indicate that the user is now registered.
        userRegistrationHeaders = StompHeaderAccessor.wrap(reply);
        assertEquals("/topic/user-status-update", userRegistrationHeaders.getDestination());
        readAndVerifyUserStatusUpdateMessage(new String((byte[])reply.getPayload(), Charset.forName("UTF-8")),
                                             TEST_HANDLE1, TEST_NAME1, InOutBoardStatus.UNREGISTERED.getDescription(),
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
                                             TEST_HANDLE1, TEST_NAME1, InOutBoardStatus.REGISTERED.getDescription(),
                                             "");


        UserStatusUpdateMessage newStatus = new UserStatusUpdateMessage(TEST_HANDLE1, TEST_NAME1,
                                                                        InOutBoardStatus.AVAILABLE, "At my desk");

        stompHeaders = StompHeaderAccessor.create(StompCommand.SEND);
        stompHeaders.setDestination("/app/user-status-update");
        stompHeaders.setSessionId("0");
        stompHeaders.setUser(new DummyPrincipal(TEST_HANDLE1));
        stompHeaders.setSessionAttributes(new HashMap<>(1));
        org.springframework.messaging.Message<byte[]> message = MessageBuilder.createMessage(new ObjectMapper().writeValueAsBytes(newStatus),
                                                                                             stompHeaders.getMessageHeaders());
        brokerChannelInterceptor.setIncludedDestinations("/topic/**");
        brokerChannelInterceptor.startRecording();

        clientInboundChannel.send(message);
        reply = brokerChannelInterceptor.awaitMessage(5);
        assertNotNull("Reply was null", reply);

        stompHeaders = StompHeaderAccessor.wrap(reply);
        assertEquals("/topic/user-status-update", stompHeaders.getDestination());
        readAndVerifyUserStatusUpdateMessage(new String((byte[])reply.getPayload(), Charset.forName("UTF-8")),
                                             TEST_HANDLE1, TEST_NAME1, InOutBoardStatus.AVAILABLE.getDescription(),
                                             "At my desk");
    }

    private void verifyRegisterSingleUser(String handle, UserRegistrationMessage userRegistrationMessage) throws Exception {
        String payload = new String(new ObjectMapper().writeValueAsBytes(userRegistrationMessage));
        mockMvc.perform(put("/inoutboard-rest/user/{handle}", handle).contentType(contentType).content(payload)).andExpect(status().isCreated());

    }

    private void verifyUnregisterSingleUser(String handle) throws Exception {
        String payload = new String(new ObjectMapper().writeValueAsBytes(new UserUnregistrationMessage(handle)));
        mockMvc.perform(delete("/inoutboard-rest/user/{handle}", handle).contentType(contentType).content(payload)).andExpect(status().isNoContent());
    }

    private UserStatusUpdateMessage readAndVerifyUserStatusUpdateMessage(String json, String handle, String name, String status, String comment) throws Exception {
        if (handle != null)
            new JsonPathExpectationsHelper("$.handle").assertValue(json, handle);
        if (name != null)
            new JsonPathExpectationsHelper("$.name").assertValue(json, name);
        if (status != null)
            new JsonPathExpectationsHelper("$.inOutBoardStatus").assertValue(json, status);
        if (comment != null)
            new JsonPathExpectationsHelper("$.comment").assertValue(json, comment);

        return new ObjectMapper().readValue(json, UserStatusUpdateMessage.class);
    }
}
