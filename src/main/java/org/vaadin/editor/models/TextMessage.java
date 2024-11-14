package org.vaadin.editor.models;

public class TextMessage {
	private String content;
	private int senderId;

	public TextMessage() {

	}

	public TextMessage(String content, int senderId) {
		this.content = content;
		this.senderId = senderId;
	}


	public String getContent() {
		return this.content;
	}

	public int getSenderId() {
		return this.senderId;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}
}
