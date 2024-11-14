package org.vaadin.editor.tokenizer;

/**
 * <p>contains all token types that can be sent to the parser</p>
 * <p>each type has a list of symbols associated with it that can appear in the string</p>
 */
public enum TokenType {
    TEXT(new String[]{}),
    BOLD(new String[]{"**", "__"}),
    ITALICS(new String[]{"*", "_"}),
    STRIKETHROUGH(new String[]{"~~"}),
    HIGHLIGHT(new String[]{"=="}),
    HEADER(new String[]{"###", "##", "#"}),
    CODE_BLOCK(new String[]{"```"}),
    BREAK(new String[]{"\n"});

    public final String[] symbols;

    TokenType(String[] symbols) {
        this.symbols = symbols;
    }

    public boolean contains(String symbol) {
        for (String s : this.symbols) {
            if (symbol.equals(s)) return true;
        }
        return false;
    }
}