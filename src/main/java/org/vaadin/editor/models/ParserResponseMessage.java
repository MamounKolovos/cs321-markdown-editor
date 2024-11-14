package org.vaadin.editor.models;

public class ParserResponseMessage {
	private TextMessage original;
	private String html;
	
	public ParserResponseMessage() {

	}

	public ParserResponseMessage(TextMessage original, String html) {
		this.original = original;
		this.html = html;
	}

	public TextMessage getOriginal() {
		return this.original;
	}

	public void setOriginal(TextMessage original) {
		this.original = original;
	}

	public String getHtml() {
		return this.html;
	}

	public void setHtml(String html) {
		this.html = html;
	}
}
