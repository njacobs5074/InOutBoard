package io.highlandcows.inoutboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.highlandcows.inoutboard.message.UserRegistrationMessage;
import io.highlandcows.inoutboard.message.UserStatusUpdateMessage;
import io.highlandcows.inoutboard.message.UserUnregistrationMessage;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.messaging.support.AbstractSubscribableChannel;
import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Useful common code for controller tests.  Basically we set up various communication channels and mock objects
 * here.  Also manage the @Before and @After events so that we can clear the database on each test.
 *
 * @author highlandcows
 * @since 10/12/14
 */
public class AbstractControllerTest {

    protected static final String TEST_HANDLE1 = "njacobs5074";
    protected static final String TEST_NAME1 = "Nick Jacobs";
    protected static final String TEST_HANDLE2 = "THX1138";
    protected static final String TEST_NAME2 = "George Lucas";

    @Autowired
    protected AbstractSubscribableChannel clientOutboundChannel;

    @Autowired
    protected AbstractSubscribableChannel clientInboundChannel;

    @Autowired
    protected AbstractSubscribableChannel brokerChannel;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected EmbeddedDatabase embeddedDatabase;

    protected MediaType contentType;

    protected MockMvc mockMvc;

    protected TestChannelInterceptor brokerChannelInterceptor;

    protected TestChannelInterceptor clientOutboundChannelInterceptor;

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

        clientOutboundChannelInterceptor = new TestChannelInterceptor(false);
        clientOutboundChannel.addInterceptor(clientOutboundChannelInterceptor);

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

    protected void verifyRegisterSingleUser(String handle, UserRegistrationMessage userRegistrationMessage) throws Exception {
        String payload = new String(new ObjectMapper().writeValueAsBytes(userRegistrationMessage));
        mockMvc.perform(put("/inoutboard-rest/user/{handle}", handle).contentType(contentType).content(payload)).andExpect(status().isCreated());
    }

    protected void verifyUnregisterSingleUser(String handle) throws Exception {
        String payload = new String(new ObjectMapper().writeValueAsBytes(new UserUnregistrationMessage(handle)));
        mockMvc.perform(delete("/inoutboard-rest/user/{handle}", handle).contentType(contentType).content(payload)).andExpect(status().isNoContent());
    }

    protected UserStatusUpdateMessage readAndVerifyUserStatusUpdateMessage(String json, String handle, String status, String comment) throws Exception {
        if (handle != null)
            new JsonPathExpectationsHelper("$.handle").assertValue(json, handle);
        if (status != null)
            new JsonPathExpectationsHelper("$.inOutBoardStatus").assertValue(json, status);
        if (comment != null)
            new JsonPathExpectationsHelper("$.comment").assertValue(json, comment);

        return new ObjectMapper().readValue(json, UserStatusUpdateMessage.class);
    }
}
