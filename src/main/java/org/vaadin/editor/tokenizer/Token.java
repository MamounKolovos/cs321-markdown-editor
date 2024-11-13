package org.vaadin.editor.tokenizer;

public class Token {
    public TokenType type;
    public String value;
    /** only initialized for format tokens since those are the ones that need opening/closing context */
    public ActionType actionType;

    public Token(TokenType type, String value, ActionType actionType) {
        this.type = type;
        this.value = value;
        this.actionType = actionType;
    }

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("{type: %s, value: %s, actionType: %s}", type, value, actionType);
    }
}
