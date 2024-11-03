package org.vaadin.editor.ws;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.vaadin.editor.presence.PresenceManager;


@Component
public class WebSocketConnectionListener {

	@EventListener
	public void handleConnection(SessionConnectEvent event) {
		SimpMessageHeaderAccessor headers =SimpMessageHeaderAccessor.wrap(event.getMessage());

		PresenceManager.addUser(headers.getSessionId());
		System.out.println("Connection: " + PresenceManager.getUsers());
	}

	@EventListener
	public void handleDisconnection(SessionDisconnectEvent event) {
		SimpMessageHeaderAccessor headers =SimpMessageHeaderAccessor.wrap(event.getMessage());

		PresenceManager.removeUser(headers.getSessionId());
		System.out.println("Disconnection: " + PresenceManager.getUsers());
	}
}
