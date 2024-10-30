import java.util.Iterator;

/**
 * The Parser class.
 * @author Writesh Maulik
 */
public class Parser {
    
    /**
     * An inner node class.
     * @author Writesh Maulik
     */
    private class Node <T>{
        
        /**
         * The inner token item.
         */
        T token; 
        /**
         * A child token if needed.
         */
        T childToken;
        /**
         * The hierarchy for the token.
         */
        int level;

        /**
         * Token contstructor.
         */
        public Node(T token){
            this.token = token;
            this.childToken = null;
            int level = 0;
        }

        /**
         * Token setter.
         * @param token any token.
         */
        public void setToken(T token){
            this.token = token;
        }

        /**
         * Token getter.
         * @return the current token.
         */
        public T getToken(){
            return this.token;
        }

        /**
         * Next Token setter.
         * @param token the child token.
         */
        public void setChildToken(T token){
            this.childToken = token;
        }

        /**
         * Getter for childToken.
         * @return the child token.
         */
        public T getChildToken(){
            return this.childToken;
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

    }

    //need to add enumarations based on tokens recieved

    /*
     * An array that will act as a K-nary tree.
     */
    private Node<String>[] tree; //add typecast to token (later)

    /**
     * The capacity of the current array.
     */
    private int capacity = 0;
    

    /**
     * Tree constructor.
     */
    @SuppressWarnings("unchecked")
    public Parser(){
        
        //Token token = getNextToken();
        //should be inside if <tokenstream> != null
        this.tree = new Node[1];

        String token; //delete after token has been implemented
        
        //creates the tree root 
        if (token != null){
            Node <String>tempToken = new Node<>(token);
            tree[0] = tempToken;
            this.capacity ++;
        }

    }

    /**
     * The DFS tree treversal for parsing the tree.
     */
    public void getTree(){

    }

    /**
     * A method to determine the hierarchy of tokens.
     * @return a comparison of two tokens
     */
    public int compare(Node<String> node){
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
