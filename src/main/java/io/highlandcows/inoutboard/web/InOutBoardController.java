package io.highlandcows.inoutboard.web;

import io.highlandcows.inoutboard.message.UserRegistrationMessage;
import io.highlandcows.inoutboard.message.UserStatusUpdateMessage;
import io.highlandcows.inoutboard.model.InOutBoardStatus;
import io.highlandcows.inoutboard.model.InOutBoardUser;
import io.highlandcows.inoutboard.model.InOutBoardUserDatabase;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Spring MVC controller that handles the REST and STOMP/WebSockets interfaces.
 * As per the {code @RequestMapping} annotation, the REST interface is at
 * <code>/inoutboard-rest</code>.
 *
 * @author highlandcows
 * @since 11/11/14
 */
@Controller
@RestController
@RequestMapping("/inoutboard-rest")
public class InOutBoardController {

    private final Logger logger = Logger.getLogger(getClass());

    // This component is used so that we can send messages via STOMP/WebSockets
    @Autowired
    private SimpMessagingTemplate stompMessagingTemplate;

    @Autowired
    private InOutBoardUserDatabase inOutBoardUserDatabase;

    /**
     * Register a user and broadcast their existence. Adds them to the database.
     * Use HTTP PUT
     *
     * @return HTTP response for action.
     */
    @RequestMapping(value = "/user/{handle}", method = RequestMethod.PUT)
    public ResponseEntity<?> registerUser(@PathVariable("handle") String handle, @RequestBody UserRegistrationMessage userRegistrationMessage) {

        HttpStatus httpStatus = HttpStatus.CREATED;
        try {
            InOutBoardUser user = inOutBoardUserDatabase.getUser(handle);
            if (user != null) {
                logger.warn("Login attempt on existing user " + handle);
                httpStatus = HttpStatus.BAD_REQUEST;
            }
            else {
                user = new InOutBoardUser(handle,
                                          userRegistrationMessage.getName(),
                                          InOutBoardStatus.REGISTERED, LocalDateTime.now(), "");
                inOutBoardUserDatabase.addUser(user);
                logger.info("User added: " + inOutBoardUserDatabase.getUser(user.getHandle()));

                broadcastUserStatus(new UserStatusUpdateMessage(user.getHandle(), user.getName(), user.getStatus(), user.getComment()));
            }
        }
        catch (Exception e) {
            logger.warn("Exception during registerUser: " + userRegistrationMessage, e);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().build(true).toUri());

        return new ResponseEntity<>(null, httpHeaders, httpStatus);
    }

    /**
     * Fetch the specified user based on their handle
     * @param handle User's handle
     * @return Either a valid user or null if no such user with that name.
     */
    @RequestMapping(value = "/user/{handle}", method = RequestMethod.GET)
    public UserStatusUpdateMessage getUser(@PathVariable("handle") String handle) {

        try {
            InOutBoardUser user = inOutBoardUserDatabase.getUser(handle);
            if (user != null) {
                return new UserStatusUpdateMessage(user.getHandle(), user.getName(), user.getStatus(), user.getComment());
            }
        }
        catch (Exception e) {
            logger.warn("Exception during getUser: " + handle, e);
        }

        return null;
    }

    /**
     * Unregister a user and broadcast that they've unregistered.  Remove them from the database.
     * Uses HTTP DELETE.
     *
     * @param handle User's handle
     * @return HTTP response for the action
     */
    @RequestMapping(value = "/user/{handle}", method = RequestMethod.DELETE)
    public ResponseEntity<?> unregisterUser(@PathVariable("handle") String handle) {

        HttpStatus httpStatus = HttpStatus.NO_CONTENT;
        try {
            InOutBoardUser user = inOutBoardUserDatabase.getUser(handle);
            if (user != null) {
                inOutBoardUserDatabase.deleteUser(user);
                broadcastUserStatus(new UserStatusUpdateMessage(user.getHandle(), user.getName(), InOutBoardStatus.UNREGISTERED, ""));
                logger.info("unregisterUser unregistered: " + handle);
            }
        }
        catch (Exception e) {
            logger.warn("Exception during unregisterUser: " + handle, e);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().build(true).toUri());

        return new ResponseEntity<>(null, httpHeaders, httpStatus);
    }

    /**
     * Used by a user to update their status.
     * Uses HTTP
     *
     * @param userStatusUpdateMessage - User's updated status
     * @return HTTP response for the action
     */
    @RequestMapping(value = "/user-status-update", method = RequestMethod.POST)
    public ResponseEntity<?> updateUserState(@RequestBody UserStatusUpdateMessage userStatusUpdateMessage) {

        HttpStatus httpStatus = HttpStatus.OK;
        try {
            logger.info("Updating user status: " + userStatusUpdateMessage);
            InOutBoardUser user = inOutBoardUserDatabase.getUser(userStatusUpdateMessage.getHandle());
            if (user != null) {
                inOutBoardUserDatabase.updateUser(user,
                                                  userStatusUpdateMessage.getInOutBoardStatus(),
                                                  userStatusUpdateMessage.getComment());
                broadcastUserStatus(userStatusUpdateMessage);
            }
            else {
                httpStatus = HttpStatus.BAD_REQUEST;
            }
        }
        catch (Exception e) {
            logger.warn("Exception during updateUserState: " + userStatusUpdateMessage, e);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder.fromCurrentRequest().build(true).toUri());

        return new ResponseEntity<>(null, httpHeaders, httpStatus);
    }

    /**
     * Retrieve a snapshot of all of the users and their status.
     *
     * @return Array containing all of the current users.
     */
    @RequestMapping(value = "/get-all-users", method = RequestMethod.GET)
    public UserStatusUpdateMessage[] getAllUsers() {

        return inOutBoardUserDatabase.getAllUsers().stream()
                                     .map(user -> new UserStatusUpdateMessage(user.getHandle(),
                                                                              user.getName(),
                                                                              user.getStatus(),
                                                                              user.getComment()))
                                     .toArray(UserStatusUpdateMessage[]::new);

    }

    /**
     * Retrieve the list of user statuses via HTTP.
     *
     * @return Array of all non-system user status values.
     */
    @RequestMapping(value = "/user-status-values", method = RequestMethod.GET)
    public InOutBoardStatus[] getUserStatusValues() {
        return Arrays.asList(InOutBoardStatus.values()).stream()
                     .filter(status -> !status.isSystemStatus())
                     .toArray(InOutBoardStatus[]::new);
    }

    /**
     * Internal function to send out the status for a given user via STOMP/WebSockets.
     */
    private void broadcastUserStatus(UserStatusUpdateMessage userStatusUpdateMessage) {
        try {
            stompMessagingTemplate.convertAndSend("/topic/user-status-update", userStatusUpdateMessage);
            logger.info("Sent '/topic/user-status-update: " + userStatusUpdateMessage);
        }
        catch (Exception e) {
            logger.error(e);
        }
    }
}