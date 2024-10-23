package org.vaadin.editor.presence;

import java.util.HashSet;

public class PresenceManager {
	private static HashSet<String> users = new HashSet<>();
	private static String globalText = "";

	public static HashSet<String> getUsers() {
		return users;
	}

	public static void addUser(String sessionId) {
		users.add(sessionId);
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
}
