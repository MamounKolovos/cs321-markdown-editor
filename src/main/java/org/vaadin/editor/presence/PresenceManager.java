package org.vaadin.editor.presence;

import java.util.HashSet;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;

@BrowserCallable
@AnonymousAllowed
public class PresenceManager {
	private static HashSet<String> users = new HashSet<>();
	private static String globalText = "";
	private static int userCount = 0;

	public static HashSet<String> getUsers() {
		return users;
	}

	public static void addUser(String sessionId) {
		users.add(sessionId);
		userCount += 1;
	}

	public static void removeUser(String sessionId) {
		users.remove(sessionId);
	}

	public static void setGlobalText(String text) {
		globalText = text;
	}

	public static String getGlobalText() {
		return globalText;
	}

	public int generateUserId() {
		return PresenceManager.userCount;
	}
}
