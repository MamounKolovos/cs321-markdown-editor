package org.vaadin.editor.parser;

import java.util.ArrayList;
import org.vaadin.editor.tokenizer.*;


abstract class Node<T extends Node<?>> {
    String type;
    String tagName; //html tag name: em, strong, etc
    ArrayList<T> children;

    String getType() {
        return this.type;
    }

    String getTagName() {
        return this.tagName;
    }

    ArrayList<T> getChildren() {
        return this.children;
    }

    void setChildren(ArrayList<T> children) {
        this.children = children;
    }
}

abstract class ContentNode extends Node<PhrasingContent>{
    // ArrayList<PhrasingContent> children;
}

// type FlowContent = Blockquote | Code | Heading | Html | List | ThematicBreak | Definition | Paragraph
abstract class FlowContent extends ContentNode{
}

//type PhrasingContent = Break | Emphasis | Html | Image | ImageReference | InlineCode | Link | LinkReference | Strong | Text
abstract class PhrasingContent extends ContentNode{
}



final class TextNode extends PhrasingContent {
    String type = "text";
    String value;

    @Override
    public String toString() {
        return String.format("textnode value: %s", this.value);
    }

    String getType() {
        return this.type;
    }
}

class RootNode extends Node<FlowContent>{
    String type = "root";
    String tagName = "html";
    // ArrayList<FlowContent> children;

    String getType() {
        return this.type;
    }

    String getTagName() {
        return this.tagName;
    }
}

class ParagraphNode extends FlowContent {
    String type = "paragraph";
    String tagName = "p";

    String getType() {
        return this.type;
    }

    String getTagName() {
        return this.tagName;
    }
}

final class StrongNode extends PhrasingContent{
    String type = "strong";
    String tagName = "strong";

    String getType() {
        return this.type;
    }

    String getTagName() {
        return this.tagName;
    }
}

final class EmphasisNode extends PhrasingContent{
    String type = "emphasis";
    String tagName = "em";

    String getType() {
        return this.type;
    }
    
    String getTagName() {
        return this.tagName;
    }
}

final class StrikeNode extends PhrasingContent{
    String type = "strike";
    String tagName = "strike";

    String getType() {
        return this.type;
    }

    String getTagName() {
        return this.tagName;
    }
}

//highlight
final class MarkNode extends PhrasingContent{
    String type = "mark";
    String tagName = "mark";

    String getType() {
        return this.type;
    }

    String getTagName() {
        return this.tagName;
    }
}

public class Parser {

    String string;
    Tokenizer tokenizer;
    RootNode root;
    private Token lookahead;

    public Parser(String string) {
        this.string = string;
        this.tokenizer = new Tokenizer(string);
    }

    private Token eat(TokenType type) {
        Token token = this.lookahead;

        if (token == null) {
            throw new Error("tried to eat null lookahead");
        }

        if (token.type != type) {
            throw new Error(String.format("types do not match: %s != %s", token.type, type));
        }

        this.lookahead = this.tokenizer.getNextToken();
        return token;
    }

    /** public entry point for parser */
    public void parse() {
        this.lookahead = this.tokenizer.getNextToken();
        this.root = this.start();
    }

    /** actual parser recursive logic */
    private RootNode start() {
        RootNode root = new RootNode();
        ArrayList<FlowContent> contents = new ArrayList<>();
        while (this.lookahead != null) {
            FlowContent content = this.content();
            contents.add(content);
        }
        // root.children = contents;
        root.setChildren(contents);
        return root;
    }

    private FlowContent content() {
        ParagraphNode paragraph = new ParagraphNode();
        ArrayList<PhrasingContent> contents = new ArrayList<>();
        while (this.lookahead != null) {
            if (this.lookahead.type == TokenType.BREAK) {
                this.eat(TokenType.BREAK);
                break;
            }
            PhrasingContent content = this.phrasingContent();
            contents.add(content);
        }
        paragraph.children = contents;
        return paragraph;
    }

    private PhrasingContent phrasingContent() {
        switch (this.lookahead.type) {
            case TEXT:
                return this.text();
            case BOLD:
                return this.strong();
            case ITALICS:
                return this.emphasis();
            case STRIKETHROUGH:
                return this.strikethrough();
            case HIGHLIGHT:
                return this.mark();
            default:
                throw new Error("token is not handled yet");
        }
    }

    /** reminder for myself to put while loop since multiple text tokens can happen */
    private TextNode text() {
        TextNode text = new TextNode();
        text.value = this.eat(TokenType.TEXT).value;
        text.children = null;
        return text;
    }

    private StrongNode strong() {
        this.eat(TokenType.BOLD);
        StrongNode node = new StrongNode();
        ArrayList<PhrasingContent> contents = new ArrayList<>();
        while (
            this.lookahead != null && 
            //continue recursing if the next token is not bold OR not closing
            //if the next token is bold AND closing, that means we dont need to recurse further and can close the bold tag
            (this.lookahead.type != TokenType.BOLD || this.lookahead.actionType != ActionType.CLOSE)
        ) {
            PhrasingContent content = this.phrasingContent();
            contents.add(content);
        }
        this.eat(TokenType.BOLD);
        node.children = contents;
        return node;
    }

    private EmphasisNode emphasis() {
        this.eat(TokenType.ITALICS);
        EmphasisNode node = new EmphasisNode();
        ArrayList<PhrasingContent> contents = new ArrayList<>();
        while (
            this.lookahead != null && 
            //continue recursing if the next token is not italics OR not closing
            (this.lookahead.type != TokenType.ITALICS || this.lookahead.actionType != ActionType.CLOSE)
        ) {
            PhrasingContent content = this.phrasingContent();
            contents.add(content);
        }
        this.eat(TokenType.ITALICS);
        node.children = contents;
        return node;
    }

    private StrikeNode strikethrough() {
        this.eat(TokenType.STRIKETHROUGH);
        StrikeNode node = new StrikeNode();
        ArrayList<PhrasingContent> contents = new ArrayList<>();
        while (
            this.lookahead != null && 
            (this.lookahead.type != TokenType.STRIKETHROUGH || this.lookahead.actionType != ActionType.CLOSE)
        ) {
            PhrasingContent content = this.phrasingContent();
            contents.add(content);
        }
        this.eat(TokenType.STRIKETHROUGH);
        node.children = contents;
        return node;
    }

    private MarkNode mark() {
        this.eat(TokenType.HIGHLIGHT);
        MarkNode node = new MarkNode();
        ArrayList<PhrasingContent> contents = new ArrayList<>();
        while (
            this.lookahead != null && 
            (this.lookahead.type != TokenType.HIGHLIGHT || this.lookahead.actionType != ActionType.CLOSE)
        ) {
            PhrasingContent content = this.phrasingContent();
            contents.add(content);
        }
        this.eat(TokenType.HIGHLIGHT);
        node.children = contents;
        return node;
    }

    public String convertToHtml() {
        return Converter.convertParseTree(this.root);
    }

    public static void main(String[] args) {
        Parser parser = new Parser("ok\nhi");
        parser.parse();

        System.out.println(PrintTree.printParseTree(parser.root));
        System.out.println(parser.convertToHtml());
    }
}

class Converter {

    /**
     * Pretty print the directory tree and its file names.
     * 
     * @param root
     *            must be a folder.
     * @return
     */
    public static String convertParseTree(RootNode root) {
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        convertParseTree(root, indent, sb);
        return sb.toString();
    }
    
    private static <T extends Node<?>> void convertParseTree(Node<T> node, int indent,
            StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("<");
        sb.append(node.getTagName());
        sb.append(">");
        sb.append("\n");
        for (T child : node.children) {
            if (child instanceof TextNode) {
                printNode(child, indent + 1, sb);
            } else {
                convertParseTree(child, indent + 1, sb);
            }
        }

        sb.append(getIndentString(indent));
        sb.append("</");
        sb.append(node.getTagName());
        sb.append(">");
        sb.append("\n");
    }
    
    private static <T extends Node<?>> void printNode(Node<T> node, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append(((TextNode)node).value);
        sb.append("\n");
    }
    
    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }
}


class PrintTree {

    /**
     * Pretty print the directory tree and its file names.
     * 
     * @param root
     *            must be a folder.
     * @return
     */
    public static String printParseTree(RootNode root) {
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printParseTree(root, indent, sb);
        return sb.toString();
    }

    private static <T extends Node<?>> void printParseTree(Node<T> node, int indent,
            StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(node.getType());
        sb.append("/");
        sb.append("\n");
        for (T child : node.children) {
            if (child instanceof TextNode) {
                printNode(child, indent + 1, sb);
            } else {
                printParseTree(child, indent + 1, sb);
            }
        }

    }

    private static <T extends Node<?>> void printNode(Node<T> node, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        String value = ((TextNode)node).value;
        if (value.equals("\n")) value = "\\n";
        sb.append(node.getType() + ": " + "\"" + value + "\"");
        sb.append("\n");
    }

    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }
}