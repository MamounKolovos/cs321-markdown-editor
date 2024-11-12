package org.vaadin.editor.parser;

import java.util.ArrayList;
import org.vaadin.editor.tokenizer.*;


abstract class Node<T extends Node<?>> {
    String type;
    ArrayList<T> children;

    String getType() {
        return this.type;
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
    // ArrayList<FlowContent> children;

    String getType() {
        return this.type;
    }
}

class ParagraphNode extends FlowContent {
    String type = "paragraph";

    String getType() {
        return this.type;
    }
}

final class StrongNode extends PhrasingContent{
    String type = "strong";

    String getType() {
        return this.type;
    }
}

final class EmphasisNode extends PhrasingContent{
    String type = "emphasis";

    String getType() {
        return this.type;
    }
}

final class StrikeNode extends PhrasingContent{
    String type = "strike";

    String getType() {
        return this.type;
    }
}

//highlight
final class MarkNode extends PhrasingContent{
    String type = "mark";

    String getType() {
        return this.type;
    }
}

public class Parser {

    Tokenizer tokenizer;
    RootNode root;
    private Token lookahead;

    public Parser(String string) {
        this.tokenizer = new Tokenizer(string);
    }

    private Token eat(TokenType type) {
        Token token = this.lookahead;

        if (token == null) {
            throw new Error("tried to eat null lookahead");
        }

        if (token.type != type) {
            throw new Error("types do not match");
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
            // case STRIKETHROUGH:
            //     return this.strikethrough();
            // case HIGHLIGHT:
            //     return this.mark();
            default:
                throw new Error("ok");
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
        while (this.lookahead != null && this.lookahead.type != TokenType.BOLD) {
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
        while (this.lookahead != null && this.lookahead.type != TokenType.ITALICS) {
            PhrasingContent content = this.phrasingContent();
            contents.add(content);
        }
        this.eat(TokenType.ITALICS);
        node.children = contents;
        return node;
    }

    // private StrikeNode strikethrough() {

    // }

    // private MarkNode mark() {

    // }

    // private void print() {
    //     RootNode traversalNode = this.root;
    //     Queue<ContentNode> bfsQueue = new LinkedList<ContentNode>();
        
    //     for (int i = 0; i < traversalNode.children.size(); i++) {
    //         bfsQueue.add(traversalNode.children.get(i));
    //     }

    //     while (bfsQueue.size() != 0) {
    //         ContentNode node = bfsQueue.poll();

    //         if (node instanceof TextNode) {
    //             System.out.format("type: %s, value: %s\n", node.getType(), ((TextNode)node).value);
    //             continue;
    //         }

    //         System.out.format("type: %s\n", node.getType());

    //         for (int i = 0; i < node.children.size(); i++) {
    //             bfsQueue.add(node.children.get(i));
    //         }
    //     }
    // }
    
    // public static void main(String[] args) {
    //     Parser parser = new Parser("hello");
    //     parser.parse();
    //     parser.print();
    // }
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
    sb.append(node.getType() + ": " + "\"" + ((TextNode)node).value + "\"");
    sb.append("\n");
}

private static String getIndentString(int indent) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < indent; i++) {
        sb.append("|  ");
    }
    return sb.toString();
}

public static void main(String[] args) {

    // Parser parser = new Parser("**1 **2** 3**");
    Parser parser = new Parser("***hey***");
    parser.parse();
    String result = printParseTree(parser.root);
    System.out.println(result);
}
}