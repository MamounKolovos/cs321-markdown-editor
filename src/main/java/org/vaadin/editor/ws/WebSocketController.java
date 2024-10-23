package org.vaadin.editor.ws;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;


@Controller
public class WebSocketController {
	@MessageMapping("/textupdate")	// handle messages sent to /textupdate
	@SendTo("/broadcasts/text")	// broadcast to /broadcasts/text
	public String textUpdate(String text) {
		return HtmlUtils.htmlEscape(text);
	}
}
