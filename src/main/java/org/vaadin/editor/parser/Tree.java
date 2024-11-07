package org.vaadin.editor.parser;

//import java.util.Iterator;
import org.vaadin.editor.tokenizer.Tokenizer;
import org.vaadin.editor.tokenizer.Token;

/**
 * The Parser class.
 * @author Writesh Maulik
 */
public class Tree {
    
    /**
     * An inner node class.
     * @author Writesh Maulik
     */
    private class Node <T>{
        
        /**
         * The inner token item.
         */
        T node;

        /**
         * A child token if needed.
         */
        T childNode;

        /**
         * The hierarchy for the token.
         */
        int level;

        /**
        * The size of the current array.
        */
        private int size;

        /**
         * Token contstructor.
         */
        public Node(T node){
            this.node = node;
            this.childNode = null;
            this.level = 0;
            this.size = 1;
        }

        /**
         * Token setter.
         * @param token any token.
         */
        public void setNode(T node){
            this.node = node;
        }

        /**
         * Token getter.
         * @return the current token.
         */
        public T getNode(){
            return this.node;
        }

        /**
         * Next Token setter.
         * @param token the child token.
         */
        public void setChild(T child){
            this.childNode = child;
        }

        /**
         * Getter for childToken.
         * @return the child token.
         */
        public T getChild(){
            return this.childNode;
        }

        /**
         * Setter for level.
         * @param level level of token to be set.
         */
        public void setLevel(int level){
            this.level = level;
        }

        /**
         * Getter for level.
         * @return the current level.
         */
        public int getLevel(){
            return this.level;
        }

        /**
         * Setter for size.
         * @param size the new size, used to incriment
         */
        public void setSize(int size){
            this.size = size;
        }

        /**
         * Getter for size.
         * @return the size of the current node.
         */
        public int getSize(){
            return this.size;
        }

    }

    //need to add enumarations based on tokens recieved

    /*
     * A pointer to the root of the tree
     */
    private Node<Token []> tree;

    /**
     * Tree constructor.
     */
    public Tree(String userString){
        
        Tokenizer tokenString = new Tokenizer(userString);
        Token rootToken = tokenString.getNextToken();

        if(rootToken != null){
            Token [] root = new Token[1];   //empty array to store tokens
            root[0] = rootToken;            //places non empty token into empty token array
            this.tree = new Node<>(root);   //makes a new node using root array

            Token nextToken = tokenString.getNextToken();
            if (nextToken != null){
            this.makeTree(nextToken, tree, 0);
            }
        }

    }

    /**
     * Private method called by the constructor to make the tree.
     */
    private void makeTree(Token token, Node<Token []> currLevel, int currIndex){

        int comparison = 0;

        if(token != null){
            Token [] currArr = currLevel.getNode();
            Token lastToken = currArr[currIndex];
            comparison = this.compare(token, lastToken);
            
            if (comparison == 0){ //the case where two tokens are of equal level
                
                if(currArr.length == currLevel.getSize()) { //if the current array is full
                    Token [] tempArr = new Token[currArr.length*2];
                    for(int i=0; i<currArr.length; i++){
                        tempArr[i] = currArr[i];
                    }
                    currArr = tempArr;
                    currArr[currIndex+1] = token;
                    currLevel.setNode(currArr);
                }
                else{ //if the current array has room
                    currArr[currIndex+1] = token;
                    currLevel.setNode(currArr);
                }
                currLevel.setSize(currLevel.getSize()+1);
            }
            else if (comparison > 0){ //the case where the current token has a higher presidence than the row its on

            }
            else { //the case where the current token has lower presidence than the row its on

            }
            
        }

    }

   /**
    * A method for DFS treversing the tree.
    * Used primarily for debugging
    */
    public void seeTree(){

    }

    /**
     * A method to determine the hierarchy of tokens.
     * @return a comparison of two tokens
     */
    public int compare(Token currToken, Token prevToken){
        int comparison = 0;

        return comparison;
    }

    /**
     * The main method.
     * This will be used for debugging.
     * @param args command line arguments.
     */
    public static void main(String[] args) {

    }

}
