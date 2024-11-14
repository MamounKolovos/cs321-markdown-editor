package org.vaadin.editor.ws;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.vaadin.editor.models.ParserResponseMessage;
import org.vaadin.editor.models.TextMessage;
import org.vaadin.editor.presence.PresenceManager;
import org.vaadin.editor.parser.Parser;


@Controller
public class WebSocketController {

	@MessageMapping("/update")
	@SendTo("/broadcasts/updates")	// anything returned will be broadcast to this endpoint
	public ParserResponseMessage onTextUpdate(TextMessage text) throws Exception {
		String content = text.getContent();

		PresenceManager.setGlobalText(content);

		System.out.println(PresenceManager.getGlobalText());

		Parser parser = new Parser(content);
		parser.parse();
		String html = parser.convertToHtml();
		ParserResponseMessage response = new ParserResponseMessage(text, html);

		return response;	// broadcast to /broadcasts/updates
	}
}
