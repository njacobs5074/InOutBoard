package io.highlandcows.inoutboard.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.annotation.support.SimpAnnotationMethodMessageHandler;

import java.util.List;

/**
 * Config that gets rid of unnecessary message handlers used during {@link io.highlandcows.inoutboard.InOutBoardControllerTest}
 *
 * @author highlandcows
 * @since 14/11/14
 */
@Configuration
public class UnregisterMessageHandlersConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private List<SubscribableChannel> channels;

    @Autowired
    private List<MessageHandler> handlers;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent unused) {
        for (MessageHandler handler : handlers) {
            if (handler instanceof SimpAnnotationMethodMessageHandler)
                continue;

            for (SubscribableChannel channel : channels)
                channel.unsubscribe(handler);
        }
    }
}
