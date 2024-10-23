package org.vaadin.editor.endpoints;

import java.util.Set;

import org.vaadin.editor.presence.PresenceManager;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

@BrowserCallable
@AnonymousAllowed
public class SessionsEndpoint {
	public Set<String> getActiveUsers() {
		return PresenceManager.getUsers();
	}
}
