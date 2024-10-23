package org.vaadin.editor.endpoints;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@Component
public class SessionManager implements HttpSessionListener {
	private static final Set<String> activeUsers = Collections.synchronizedSet(new HashSet<>());

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		activeUsers.add(event.getSession().getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		activeUsers.remove(event.getSession().getId());
		System.out.println(event.getSession().getId() + " has disconnected");
	}


	public static Set<String> getActiveUsers() {
		System.out.println(activeUsers);
		return activeUsers;
	}
}
