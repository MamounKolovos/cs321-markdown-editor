package org.vaadin.editor.presence;

import java.util.HashSet;

import org.vaadin.editor.models.ParserResponseMessage;
import org.vaadin.editor.models.TextMessage;
import org.vaadin.editor.parser.Parser;

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

	public ParserResponseMessage getInitialText(int userId) {
		String text = PresenceManager.getGlobalText();
		System.out.println("user " + userId + " connected, sending text: '" + text + "'");
		TextMessage message = new TextMessage(text, userId);
		Parser parser = new Parser(text);
		parser.parse();
		ParserResponseMessage response = new ParserResponseMessage(message, parser.convertToHtml());

		return response;
	}
}
