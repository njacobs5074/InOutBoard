package io.highlandcows.inoutboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.highlandcows.inoutboard.message.RegisteredUsersRequestMessage;
import io.highlandcows.inoutboard.message.UserRegistrationMessage;
import io.highlandcows.inoutboard.message.UserStatusUpdateMessage;
import io.highlandcows.inoutboard.message.UserUnregistrationMessage;
import io.highlandcows.inoutboard.model.InOutBoardStatus;
import org.junit.Test;
import org.springframework.test.util.JsonPathExpectationsHelper;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Test JSON serialization of messages.  Import because the client code assumes these formats.
 *
 * @author highlandcows
 * @since 16/11/14
 */
public class JSONSerializationTest {

    @Test
    public void testUserRegistrationJSONSerialization() throws Exception {
        UserRegistrationMessage msg = new UserRegistrationMessage("ABCD", "Test");
        String json = new String(new ObjectMapper().writeValueAsBytes(msg), Charset.forName("UTF-8"));
        new JsonPathExpectationsHelper("$.type").assertValue(json, UserRegistrationMessage.class.getSimpleName());
        new JsonPathExpectationsHelper("$.handle").assertValue(json, "ABCD");
        new JsonPathExpectationsHelper("$.name").assertValue(json, "Test");
    }

    @Test
    public void testUserUnregistrationJSONSerialization() throws Exception {
        UserUnregistrationMessage msg = new UserUnregistrationMessage("ABCD");
        String json = new String(new ObjectMapper().writeValueAsBytes(msg), Charset.forName("UTF-8"));
        new JsonPathExpectationsHelper("$.type").assertValue(json, UserUnregistrationMessage.class.getSimpleName());
        new JsonPathExpectationsHelper("$.handle").assertValue(json, "ABCD");
    }

    @Test
    public void testUserStatusUpdateJSONSerialization() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        UserStatusUpdateMessage msg = new UserStatusUpdateMessage("ABCD", "TEST NAME", InOutBoardStatus.AVAILABLE, "At my desk", now);
        String json = new String(new ObjectMapper().writeValueAsBytes(msg), Charset.forName("UTF-8"));
        new JsonPathExpectationsHelper("$.type").assertValue(json, UserStatusUpdateMessage.class.getSimpleName());
        new JsonPathExpectationsHelper("$.handle").assertValue(json, "ABCD");
        new JsonPathExpectationsHelper("$.name").assertValue(json, "TEST NAME");
        new JsonPathExpectationsHelper("$.inOutBoardStatus").assertValue(json, "Available");
        new JsonPathExpectationsHelper("$.comment").assertValue(json, "At my desk");
        new JsonPathExpectationsHelper("$.lastUpdated").assertValue(json, now.toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @Test
    public void testRegisterUsersRequestJSONSerialization() throws Exception {
        RegisteredUsersRequestMessage msg = new RegisteredUsersRequestMessage("ABCD");
        String json = new String(new ObjectMapper().writeValueAsBytes(msg), Charset.forName("UTF-8"));
        new JsonPathExpectationsHelper("$.type").assertValue(json, RegisteredUsersRequestMessage.class.getSimpleName());
        new JsonPathExpectationsHelper("$.handle").assertValue(json, "ABCD");
    }

    @Test
    public void testUserStatusValuesJSONSerialization() throws Exception {
        InOutBoardStatus[] statuses = InOutBoardStatus.values();
        String json = new String(new ObjectMapper().writeValueAsBytes(statuses), Charset.forName("UTF-8"));
        int i = 0;
        for (InOutBoardStatus status : InOutBoardStatus.values()) {
            new JsonPathExpectationsHelper(String.format("@[%d]", i++)).assertValue(json, status.getDescription());
        }
    }
}
