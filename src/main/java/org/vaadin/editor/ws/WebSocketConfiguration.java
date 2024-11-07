package org.vaadin.editor.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/broadcasts");	// where messages from the server will be broadcast to
		config.setApplicationDestinationPrefixes("/app");	// where messages TO the server should be sent
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// the endpoint that will be waiting to create new connections
		registry.addEndpoint("/create-ws-connection");
	}
}
