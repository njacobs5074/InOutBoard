package io.highlandcows.inoutboard.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Used in {@link io.highlandcows.inoutboard.InOutBoardControllerTest} to simulate STOMP/WebSockets.
 *
 * @author highlandcows
 * @since 14/11/14
 */
@Configuration
@EnableScheduling
@EnableAutoConfiguration
@ComponentScan(
        basePackages = "io.highlandcows.inoutboard",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Configuration.class)
)
@EnableWebSocketMessageBroker
@EnableWebMvc
public class WebSocketTestConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/inoutboard-websocks").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/queue/", "/topic/");
        registry.setApplicationDestinationPrefixes("/app");
    }

}
