package io.highlandcows.inoutboard.web;

import io.highlandcows.inoutboard.message.PingMessage;
import io.highlandcows.inoutboard.message.PongMessage;
import io.highlandcows.inoutboard.model.InOutBoardUser;
import io.highlandcows.inoutboard.model.InOutBoardUserDatabase;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

/**
 * @author highlandcows
 * @since 10/12/14
 */
@Controller
@EnableScheduling
public class PingPongMessageController {

    private Logger logger = Logger.getLogger(getClass());

    @Autowired
    private SimpMessagingTemplate stompMessagingTemplate;

    @Autowired
    private InOutBoardUserDatabase inOutBoardUserDatabase;

    @Scheduled(fixedRate = 5000)
    public void pingClients() {
        try {
            PingMessage pingMessage = new PingMessage();
            stompMessagingTemplate.convertAndSend("/topic/ping", pingMessage);
            logger.info("Sent '/topic/ping: " + pingMessage);
        }
        catch (Exception e) {
            logger.error(e);
        }
    }

    @SubscribeMapping("/pong")
    public void receiveClientPongMessage(PongMessage pongMessage) {
        logger.trace("Recv'd ping: " + pongMessage);
        try {
            InOutBoardUser user = inOutBoardUserDatabase.getUser(pongMessage.getHandle());
            if (user == null) {
                logger.warn("Ping response from unknown user: " + pongMessage.getHandle());
                return;
            }

            inOutBoardUserDatabase.updateLastUpdated(user);
        }
        catch (Exception e) {
            logger.error(e);
        }
    }
}
