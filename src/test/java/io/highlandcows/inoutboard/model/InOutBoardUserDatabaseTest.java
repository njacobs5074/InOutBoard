package io.highlandcows.inoutboard.model;

import io.highlandcows.inoutboard.config.DatabaseConfig;
import io.highlandcows.inoutboard.config.InOutBoardServerTestConfig;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests that demonstrate the CRUD operations for the {@link InOutBoardUserDatabase} work
 * correctly.
 *
 * @author highlandcows
 * @since 11/11/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InOutBoardServerTestConfig.class, DatabaseConfig.class})
public class InOutBoardUserDatabaseTest {

    @Autowired
    EmbeddedDatabase embeddedDatabase;

    @Autowired
    private InOutBoardUserDatabase inOutBoardUserDatabase;

    @After
    public void cleanUp() throws Exception {
        embeddedDatabase.getConnection().createStatement().execute("delete from inout_board_user");
    }

    @Test
    public void testAddUser() throws Exception {
        inOutBoardUserDatabase.addUser(new InOutBoardUser("1234", "Nick Jacobs"));

        InOutBoardUser user = inOutBoardUserDatabase.getUser("1234");
        assertNotNull("Expected not null user 1234", user);
        assertEquals("Expected user handle = 1234", "1234", user.getHandle());
        assertEquals("Expected user name = Nick Jacobs", "Nick Jacobs", user.getName());
        assertEquals("Expected status = REGISTERED", InOutBoardStatus.REGISTERED, user.getStatus());
    }

    @Test
    public void testAddTwoUsers() throws Exception {
        inOutBoardUserDatabase.addUser(new InOutBoardUser("1234", "Nick Jacobs"));
        inOutBoardUserDatabase.addUser(new InOutBoardUser("4567", "Highland Cow"));
        assertEquals("Expected 2 users in database", 2, inOutBoardUserDatabase.getNumberOfUsers());

        assertNotNull("Expected to find user 1234", inOutBoardUserDatabase.getUser("1234"));
        assertNotNull("Expected to find user 4567", inOutBoardUserDatabase.getUser("4567"));
    }

    @Test
    public void testAddAndDeleteUser() throws Exception {
        inOutBoardUserDatabase.addUser(new InOutBoardUser("1234", "Nick Jacobs"));
        assertEquals("Expected 1 user in database", 1, inOutBoardUserDatabase.getNumberOfUsers());

        InOutBoardUser user = inOutBoardUserDatabase.getUser("1234");
        assertNotNull("Expected not null user 1234", user);
        inOutBoardUserDatabase.deleteUser(user);
        assertEquals("Expected no users in database", 0, inOutBoardUserDatabase.getNumberOfUsers());
    }

    @Test
    public void testUpdateUser() throws Exception {
        inOutBoardUserDatabase.addUser(new InOutBoardUser("1234", "Nick Jacobs"));
        InOutBoardUser user = inOutBoardUserDatabase.getUser("1234");
        assertNotNull("Expected not null user 1234", user);

        inOutBoardUserDatabase.updateUser(user, InOutBoardStatus.AVAILABLE, "Working from home");
        InOutBoardUser user2 = inOutBoardUserDatabase.getUser("1234");
        assertEquals("Expected AVAILABLE for user 1234", InOutBoardStatus.AVAILABLE, user2.getStatus());
        assertEquals("Expected 'Working from home' comment", "Working from home", user2.getComment());
    }
}
