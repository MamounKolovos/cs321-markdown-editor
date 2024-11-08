package org.vaadin.editor.parser;

//import java.util.Iterator;
import org.vaadin.editor.tokenizer.Tokenizer;
import org.vaadin.editor.tokenizer.Token;

/**
 * The Tree class.
 * @author Writesh Maulik
 */
public class Tree {
    
    /**
     * An inner node class.
     * @author Writesh Maulik
     */
    private class Node <T>{
        
        /**
         * The inner node item.
         */
        T node;

        /**
         * A child node if needed.
         */
        Node<T> childNode;

        /**
         * Node constructor.
         */
        public Node(T node){
            this.node = node;
            this.childNode = null;
        }

        /**
         * Node setter.
         * @param node any node.
         */
        public void setNode(T node){
            this.node = node;
        }

        /**
         * Node getter.
         * @return the current node.
         */
        public T getNode(){
            return this.node;
        }

        /**
         * Next node setter.
         * @param child the child node.
         */
        public void setChild(Node<T> child){
            this.childNode = child;
        }

        /**
         * Getter for childNode.
         * @return the child node.
         */
        public Node<T> getChild(){
            return this.childNode;
        }

    }

    /**
     * A private class to keep track of arrays at different levels.
     * @author Writesh Maulik
     */
    private class Branch{

        /**
         * An Arry of Nodes.
         */
        private Node<Token>[] branch;

        /**
         * The hierarchy in the tree.
         */
        private int level;

        /**
         * The size of the current branch.
         */
        private int size;

        /**
         * The branch constructor.
         */
        @SuppressWarnings("unchecked")
        public Branch(){
            this.branch = new Node[1];
            this.level = 0;
            this.size = 0;
        }
        
        /**
         * Setter for branch.
         * @param branch a new array.
         */
        public void setBranch(Node<Token>[] branch){
            this.branch = branch;
        }

        /**
         * The getter for Branch
         * @return the branch array.
         */
        public Node<Token>[] getBranch(){
            return this.branch;
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
         * Size setter.
         * @param size
         */
        public void setSize(int size){
            this.size = size;
        }

        /**
         * Size getter.
         * @return the size member.
         */
        public int getSize(){
            return this.size;
        }
    }

    //need to add enumarations based on tokens recieved

    /*
     * A pointer to the root of the tree.
     */
    private Branch root;


    /**
     * A token input stream.
     */
    private Tokenizer tokenStream;

    /**
     * Tree constructor.
     */
    @SuppressWarnings("unchecked")
    public Tree(String userString){
        
        this.tokenStream = new Tokenizer(userString);
        Token firstToken = tokenStream.getNextToken();

        if(firstToken != null){
            this.root = new Branch();                           //initialize the tree root
            Node<Token> firstNode = new Node<>(firstToken);     //make the first index for root
            this.root.getBranch()[0] = firstNode;               //stored first index in the root array
            this.root.setSize(this.root.getSize()+1);           //incrimented the size of root array                             

            Token nextToken = tokenStream.getNextToken();
            if (nextToken != null){
            this.makeTree(nextToken, this.root.getBranch(), this.root.getBranch(), 0);
            }
        }
    }

    /**
     * Private method called by the constructor to recursively make K-nary tree.
     */
    @SuppressWarnings("unchecked")
    private void makeTree(Token token, Node<Token>[] currLevel, Node<Token>[] parent, int currIndex){

        int comparison = 0;

        if(token != null){ //checks for null tokens
        
            comparison = this.compare(token, currLevel[currIndex].getNode()); //compares levels this token with the previously inserted token
            
            if (comparison == 0){ //the case where two tokens are of equal level
                
                if(currLevel.length == currLevel[currIndex].getSize()) { //if the current array is full
                    Node<Token>[] tempArr = new Node[currLevel.length*2];
                    for(int i=0; i<currLevel.length; i++){
                        tempArr[i] = currLevel[i];
                    }
                    currLevel = tempArr;
                    Node<Token> nextTokenNode = new Node<>(token);
                    currLevel[currIndex+1] = nextTokenNode;
                }
                else{ //if the current array has room
                    currArr[currIndex+1] = token;
                    currLevel.setNode(currArr);
                }
                currLevel.setSize(currLevel.getSize()+1);
                token = this.tokenStream.getNextToken();
                currIndex ++;
                if (token != null){
                    this.makeTree(token, currLevel, currIndex);
                }
            }
            else if (comparison > 0){ //the case where the current token has a higher presidence than the row its on
                Token [] childArr = new Token[1];
                childArr[0] = token;
                Node<Token []> childNode = new Node<>(childArr);
                
                currLevel.setChild(childNode);
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
