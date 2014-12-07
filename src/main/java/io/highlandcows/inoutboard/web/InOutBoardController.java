package io.highlandcows.inoutboard.web;

import io.highlandcows.inoutboard.message.UserRegistrationMessage;
import io.highlandcows.inoutboard.message.UserStatusUpdateMessage;
import io.highlandcows.inoutboard.message.UserUnregistrationMessage;
import io.highlandcows.inoutboard.model.InOutBoardStatus;
import io.highlandcows.inoutboard.model.InOutBoardUser;
import io.highlandcows.inoutboard.model.InOutBoardUserDatabase;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Spring MVC controller that handles the REST and STOMP/WebSockets interfaces.
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
                httpStatus = HttpStatus.BAD_REQUEST;
            }
            else {
                user = new InOutBoardUser(handle,
                                          userRegistrationMessage.getName(),
                                          InOutBoardStatus.REGISTERED, "");
                inOutBoardUserDatabase.addUser(user);
                logger.info("registerUser added: " + inOutBoardUserDatabase.getUser(user.getHandle()));

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
     * @return HTTP respnse for the action
     */
    @RequestMapping(value = "/user/{handle}", method = RequestMethod.DELETE)
    public ResponseEntity<?> unregisterUser(@PathVariable("handle") String handle) {

        HttpStatus httpStatus = HttpStatus.NO_CONTENT;
        try {
            InOutBoardUser user = inOutBoardUserDatabase.getUser(handle);
            if (user != null) {
                inOutBoardUserDatabase.deleteUser(user);
                broadcastUserStatus(new UserStatusUpdateMessage(user.getHandle(), user.getName(), InOutBoardStatus.UNREGISTERED, ""));
                logger.info("unregisterUser deleted: " + handle);
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
     * Uses STOMP/WebSockets
     *
     * @param userStatusUpdateMessage
     * @return
     */
    @MessageMapping("/user-status-update")
    public UserStatusUpdateMessage updateUserState(UserStatusUpdateMessage userStatusUpdateMessage) {
        try {
            InOutBoardUser user = inOutBoardUserDatabase.getUser(userStatusUpdateMessage.getHandle());
            if (user != null) {
                inOutBoardUserDatabase.updateUser(user, userStatusUpdateMessage.getInOutBoardStatus(),
                                                  userStatusUpdateMessage.getComment());
                return new UserStatusUpdateMessage(user.getHandle(), user.getName(),
                                                   userStatusUpdateMessage.getInOutBoardStatus(),
                                                   userStatusUpdateMessage.getComment());
            }
        }
        catch (Exception e) {
            logger.warn("Exception during updateUserState: " + userStatusUpdateMessage, e);
        }
        return null;
    }

    /**
     * Retrieve a snapshot of all of the users and their status.
     *
     * @return
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
     * @return
     */
    @RequestMapping(value = "/user-status-values", method = RequestMethod.GET)
    public InOutBoardStatus[] getUserStatusValues() {
        return InOutBoardStatus.values();
    }

    /**
     * Internal function to send out the status for a given user via STOMP/WebSockets.
     *
     * @param userStatusUpdateMessage
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