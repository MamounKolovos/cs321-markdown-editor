package org.vaadin.editor.parser;

/**
 * The parser class.
 * @author Writesh Maulik
 */
public class Parser {

    /**
     * The whole K-nary tree.
     */
    private Tree tree;

    /**
     * Parser Constructor.
     * @param userString
     */
    public Parser(String userString) {
        this.tree = new Tree(userString);
    }

    /**
     * The DFS tree treversal for parsing the tree.
     * @return a K-nary tree.
     */
    public Tree getTree(){
        return this.tree;
    }
}
