package org.vaadin.editor.ws;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.vaadin.editor.models.TextMessage;
import org.vaadin.editor.presence.PresenceManager;


@Controller
public class WebSocketController {

	@MessageMapping("/test")
	@SendTo("/broadcasts/test")
	public String test(TextMessage text) throws Exception {
		String content = text.getContent();

		PresenceManager.setGlobalText(content);

		System.out.println(PresenceManager.getGlobalText());

		return content;
	}
}
