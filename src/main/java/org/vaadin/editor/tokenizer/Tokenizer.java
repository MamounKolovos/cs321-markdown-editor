package org.vaadin.editor.tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * container of helper functions that aren't tied to any class instance since they dont require state, just passed in variables
 */

final class TokenizerUtils {
    private TokenizerUtils() {
        throw new Error("Utility class, only contains static methods and should not be instantiated");
    }

    // Method to check if a character is a Unicode punctuation character
    public static boolean isUnicodePunctuation(char ch) {
        int type = Character.getType(ch);
        return type == Character.CONNECTOR_PUNCTUATION ||
               type == Character.DASH_PUNCTUATION ||
               type == Character.START_PUNCTUATION ||
               type == Character.END_PUNCTUATION ||
               type == Character.INITIAL_QUOTE_PUNCTUATION ||
               type == Character.FINAL_QUOTE_PUNCTUATION ||
               type == Character.OTHER_PUNCTUATION ||
               type == Character.MATH_SYMBOL;
    }

    // Method to check if a character is an ASCII punctuation character
    public static boolean isASCIIPunctuation(char ch) {
        return (ch >= 0x21 && ch <= 0x2F) ||  // !"#$%&'()*+,-./
               (ch >= 0x3A && ch <= 0x40) ||  // :;<=>?@
               (ch >= 0x5B && ch <= 0x60) ||  // [\]^_`
               (ch >= 0x7B && ch <= 0x7E);    // {|}~
    }

    static boolean isFormatToken(TokenType type) {
        return 
            type == TokenType.ITALICS || 
            type == TokenType.BOLD ||
            type == TokenType.HIGHLIGHT ||
            type == TokenType.STRIKETHROUGH;
    }

    public static TokenType getTokenType(String tokenValue) {
        for (TokenType tokenType : TokenType.values()) {
            for (String symbol : tokenType.symbols) {
                if (symbol.equals(tokenValue)) {
                    return tokenType;
                }
            }
        }

        // We assume tokenValue is raw text if not found in symbol lists
        return TokenType.TEXT;
    }

    static boolean isWhiteSpace(char c) {
        return
            c == 0 || //for start of line and end of line characters
            c == ' ' ||
            c == '\n' ||
            c == '\r' ||
            c == '\t';
    }
}

/**
 * <p>LEFT: ***abc</p>
 * <p>RIGHT: abc***</p>
 * <p>BOTH: abc***def</p>
 * <p>NEITHER: abc *** def</p>
 */
enum FlankDirection {
    LEFT,
    RIGHT,
    BOTH,
    NEITHER
}

enum ActionType {
    OPEN,
    CLOSE,
}

/**
 * object representation of a delimiter run
 * <p>
 * a delimiter run is a number of adjacent identical symbols such as *** or __
 * </p>
 */
class DelimRun {
    public String value;
    public ActionType actionType;
    public FlankDirection flankDir;
    /** starting index of the run (INCLUSIVE) */
    public int start;
    /** ending index of the run (EXCLUSIVE) */
    public int end;
    public int length;

    public DelimRun(String value, FlankDirection flankDir, int start, int end) {
        this.value = value;
        this.flankDir = flankDir;
        this.start = start;
        this.end = end;
        this.length = end - start;
    }

    /**
     * <p>a delimiter run symbol is the minimum number of characters needed to represent a token value</p>
     * <p>important to note that symbols are not directly associated with token types, the "*" symbol can be for both italics or bold</p>
     */
    public final static String[] symbols = {"*", "_", "~~", "=="};

    public String getTokenSymbol() {
        for (String s : DelimRun.symbols) {
            if (this.value.contains(s)) return s;
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("{value: %s, actionType: %s, flankDir: %s, start: %d, end: %d}", value, actionType, flankDir, start, end);
    }
}

public class Tokenizer {

    private Stack<Token> scheduledTokens = new Stack<>();

    private int curRunIdx;
    /** 
     * delimiter runs that have been preprocessed and filtered,
     * guaranteed to eventually construct formatting tokens NOT text tokens
     */
    private ArrayList<DelimRun> runs;

    public String string;
    private String slicedString;
    private int cursor = 0;

    /**
     * <p>used for format tokens to determine if the symbols are balanced within the string </p>
     * <p>balanced = true: **hello**</p>
     * <p>balanced = false: **hello*</p>
     */
    private boolean balanced = true;

    /**
     * helps manage the current context of the cursor, pushes all format token values onto stack and removes them once a context ends
     * for ***hello***, the first context will be bold, the second will be italics which will be represented as *hello*
     * all 3 tokenValues: *, hello, and * will get popped from the stack once the context is over so that we know the outer context is bold
     */
    private Stack<String> contextStack = new Stack<>();

    public Tokenizer(String string) {
        this.string = string;

        Pattern textPattern = Pattern.compile("(_+|\\*+|~+|=+)");
        Matcher matcher = textPattern.matcher(this.string);
        this.runs = matcher
            .results()
            .map(matchResult -> {
                String value = matchResult.group();
                int start = matchResult.start();
                int end = matchResult.end();
                FlankDirection flankDir;

                //beginning and end of string treated as whitespace
                char prevChar = start-1 < 0 ? ' ' : string.charAt(start-1);
                char nextChar = end > string.length() - 1 ? ' ' : string.charAt(end); //end is inclusive which is why we dont do end+1

                //flank algorithms taken directly from commonmark spec

                boolean isLeftFlanking = 
                    !TokenizerUtils.isWhiteSpace(nextChar) && 
                    (!TokenizerUtils.isUnicodePunctuation(nextChar) ? true : TokenizerUtils.isWhiteSpace(prevChar) || TokenizerUtils.isUnicodePunctuation(prevChar));
                
                boolean isRightFlanking = 
                    !TokenizerUtils.isWhiteSpace(prevChar) &&
                    (!TokenizerUtils.isUnicodePunctuation(prevChar) ? true : TokenizerUtils.isWhiteSpace(nextChar) || TokenizerUtils.isUnicodePunctuation(nextChar));

                if (isLeftFlanking && isRightFlanking) {
                    flankDir = FlankDirection.BOTH;
                } else if (isLeftFlanking) {
                    flankDir = FlankDirection.LEFT;
                } else if (isRightFlanking) {
                    flankDir = FlankDirection.RIGHT;
                } else {
                    flankDir = FlankDirection.NEITHER;
                }
                return new DelimRun(value, flankDir, start, end);
            })
            .collect(Collectors.toCollection(ArrayList<DelimRun>::new));


        // System.out.println(this.string);

        // System.out.println("before");
        // System.out.println(String.join("\n", this.runs.stream().map(r -> r.toString()).toArray(String[]::new)));
        // System.out.println();

        /**
         * Pre-processing and filtering
         */

        this.preprocessEscapeChars();

        this.runs = this.runs.stream()
        // filter out runs that cannot be converted into tokens
        .filter(run -> {
            String symbol = run.getTokenSymbol();
            if (
                (symbol == null) ||
                (symbol.equals("*") && run.length > 3) ||
                ((symbol.equals("~~") || symbol.equals("==")) && run.length > 2)
            ) {
                return false;
            }

            return true;
        }).collect(Collectors.toCollection(ArrayList<DelimRun>::new));

        this.assignActionTypes();

        // System.out.println();
        // System.out.println("after");
        // System.out.println(String.join("\n", this.runs.stream().map(r -> r.toString()).toArray(String[]::new)));
        // System.out.println();
    }

    private void assignActionTypes() {
        ArrayList<DelimRun> openRuns = new ArrayList<>();

        for (int i = 0; i < this.runs.size(); i++) {
            DelimRun run = this.runs.get(i);
            // System.out.format("BEFORE: %s\n", run);
            switch (run.flankDir) {
                case LEFT:
                    run.actionType = ActionType.OPEN;
                    openRuns.add(run);
                    break;
                case RIGHT:
                    run.actionType = ActionType.CLOSE;
                    break;
                case BOTH:
                    Optional<DelimRun> openMatch = openRuns.stream()
                        .filter(r -> run.value.equals(r.value))
                        .findFirst();
                    if (openMatch.isPresent()) {
                        run.actionType = ActionType.CLOSE;
                        //once open run has been matched, you remove it from openRuns so it doesn't get matched again
                        openRuns.remove(openMatch.get());
                    } else {
                        run.actionType = ActionType.OPEN;
                        openRuns.add(run);
                    }
                    break;
                case NEITHER:
                    break;
            }
            // System.out.format("AFTER: %s\n", run);
            // System.out.println();
        }
    }

    /**
     * <p>when a symbol is escaped, it's treated as a normal text character instead of an actual formatting symbol</p>
     * 
     * <p>runs are only used for helping the tokenizer handle formatting symbols,
     *   therefore we need to loop through the string and modify the runs based on if they contain any escaped symbols</p>
     * 
     * <p>if theres an even number of escape characters before a symbol, that means they escape each other and the symbol should act as a formatting symbol</p>
     * <p>if theres an odd number of escape characters before a symbol, that means theres always one escape character left to pair with the symbol and make it normal text</p>
     */
    private void preprocessEscapeChars() {
        if (!this.hasRuns()) return;

        int escapeCount = 0;
        int runIdx = 0;
        for (int i = 0, n = this.string.length(); i < n; i++) {
            if (runIdx < this.runs.size() && i >= this.runs.get(runIdx).end) {
                runIdx++;
            }
            
            char curChar = this.string.charAt(i);
            if (curChar == '\\') {
                escapeCount++;
                continue;
            }
            
            // if (!TokenizerUtils.isFormatToken(TokenizerUtils.getTokenType(String.valueOf(curChar)))) continue;
            if (curChar != '*' && curChar != '_' && curChar != '~' && curChar != '=') continue;
            // System.out.format("%d, %d\n", i, this.runs.get(runIdx).start);
            
            if (escapeCount % 2 != 0) {
                DelimRun run = this.runs.get(runIdx);
                if (run.length == 1) {
                    this.runs.remove(runIdx);
                    run = null;
                } else {
                    run.value = run.value.substring(1);
                    run.start++;
                    run.length--;
                    //we only care about first symbol of the run, once that symbol has been processed we can skip the rest of the run in the string
                    i += run.length;
                }
                escapeCount = 0;
            }
        }
    }

    private boolean hasRuns() {
        return this.runs.size() > 0;
    }

    private DelimRun getCurDelimiterRun() {
        if (!this.hasRuns()) return null;
        return this.runs.get(this.curRunIdx);
    }

    private DelimRun getRunAtPos(int pos) {
        for (DelimRun run : this.runs) {
            if (pos >= run.start && pos < run.end) {
                return run;
            }
        }

        return null;
    }

    private ArrayList<DelimRun> getRunEquivalenceGroup() {
        if (this.curRunIdx >= this.runs.size() - 1) return null;
        ArrayList<DelimRun> group = new ArrayList<>();

        int openCount = 0;
        int closeCount = 0;
        int i = this.curRunIdx;
        DelimRun startRun = this.getCurDelimiterRun();

        do {
            DelimRun run = this.runs.get(i);

            //equivalence group can only contain runs with matching symbols, so only asterisks for example
            if (startRun.getTokenSymbol().equals(run.getTokenSymbol())) {
                if (run.actionType == ActionType.OPEN) openCount += run.length;
                if (run.actionType == ActionType.CLOSE) closeCount += run.length;
                group.add(run);
            }
            i++;
        } while (i < this.runs.size() && (group.size() < 2 || openCount != closeCount));

        //run equivalence group could not be found because all runs were searched and the counts still arent equal
        if (i == this.runs.size() && openCount != closeCount) {
            return null;
        }

        return group;
    }

    private String getTextTokenValue() {
        int slicePos = 0;
        //loop through string instead of sliced string so indexes are accurate for the getRunAtPos() calls
        for (int i = this.cursor; i < this.string.length(); i++) {
            char curChar = this.string.charAt(i);

            if (
                curChar == '\\' ||
                ((curChar == '*' || curChar == '_') && this.balanced && this.getRunAtPos(i) != null) ||
                (curChar == '~' && this.balanced && this.getRunAtPos(i) != null) ||
                (curChar == '=' && this.balanced && this.getRunAtPos(i) != null)
            ) break;

            slicePos++;
        }
        return this.slicedString.substring(0, slicePos);
    }

    public boolean hasMoreTokens() {
        return this.cursor != this.string.length();
    }

    private void updateState(TokenType tokenType, String tokenValue) {
        if (!this.hasMoreTokens()) return;

        //if new token value is the same as the last token value on the stack, that means the context has ended and needs to be removed
        if (this.contextStack.size() != 0 && tokenValue.equals(this.contextStack.peek())) {
            this.contextStack.pop();
        } else {
            if (this.balanced && TokenizerUtils.isFormatToken(tokenType)) this.contextStack.push(tokenValue.toString());
        }

        /**
         * if cursor has made it past the current run,
         * we don't know how many runs the cursor has passed, so we keep incrementing the run index until we "catch up" to the cursor
         */
        while (
            this.curRunIdx < this.runs.size() && //safety check
            this.cursor >= this.getCurDelimiterRun().end
        ) {
            this.curRunIdx++;
        }
    }

    public Token getNextToken() {
        if (!this.hasMoreTokens()) return null;
        if (this.scheduledTokens.size() != 0) {
            Token token = this.scheduledTokens.pop();
            this.cursor += token.value.length();
            this.updateState(token.type, token.value);
            return token;
        }

        TokenType tokenType = null;
        String tokenValue = null;
        //gets the part of the string that hasnt been tokenized yet
        this.slicedString = this.string.substring(this.cursor);
        
        char startChar = this.slicedString.charAt(0);
        String startCharAsStr = String.valueOf(startChar);
        switch (startChar) {
            /*
             *  raw: \\ => str: "\\\\" => render: \
             *  raw: \ => str: "\\" => render: \
             *  raw: \* => str: "\\*" => render: *
             *  raw: \g => str: "\\g" => render: \g
             */
            case '\\':
                if (this.slicedString.length() >= 2) {
                    char nextChar = this.slicedString.charAt(1); //Example: the "a" in \a
                    if (TokenizerUtils.isASCIIPunctuation(nextChar)) {
                        tokenType = TokenType.TEXT;
                        tokenValue = this.slicedString.substring(1, 2);
                        this.cursor += 2;
                    } else {
                        tokenType = TokenType.TEXT;
                        tokenValue = this.slicedString.substring(0, 2);
                        this.cursor += 2;
                    }
                } else {
                    tokenType = TokenType.TEXT;
                    tokenValue = this.slicedString.substring(0, 1);
                    this.cursor += 1;
                }
                break;
            case '=': {
                DelimRun curRun = this.getCurDelimiterRun();
                if (curRun == null) break;
                if (this.getRunAtPos(this.cursor) == null) break;

                if (this.balanced && !this.contextStack.empty() && curRun.actionType == ActionType.CLOSE) {
                    String value = this.contextStack.peek();
                    Token token = new Token(TokenizerUtils.getTokenType(value), value);
                    this.cursor += token.value.length();
                    this.updateState(token.type, token.value);
                    return token;
                }

                ArrayList<DelimRun> eqGroup = this.getRunEquivalenceGroup();
                this.balanced = eqGroup != null;

                if (this.balanced) {
                    tokenType = TokenType.HIGHLIGHT;
                    tokenValue = "==";
                    this.cursor += 2;
                }
                break;
            }
            case '~': {
                DelimRun curRun = this.getCurDelimiterRun();
                if (curRun == null) break;
                if (this.getRunAtPos(this.cursor) == null) break;

                if (this.balanced && !this.contextStack.empty() && curRun.actionType == ActionType.CLOSE) {
                    String value = this.contextStack.peek();
                    Token token = new Token(TokenizerUtils.getTokenType(value), value);
                    this.cursor += token.value.length();
                    this.updateState(token.type, token.value);
                    return token;
                }

                ArrayList<DelimRun> eqGroup = this.getRunEquivalenceGroup();
                this.balanced = eqGroup != null;

                if (this.balanced) {
                    tokenType = TokenType.STRIKETHROUGH;
                    tokenValue = "~~";
                    this.cursor += 2;
                }
                break;
            }
            case '_':
            case '*': {
                DelimRun curRun = this.getCurDelimiterRun();
                if (curRun == null) break;

                if (this.balanced && !this.contextStack.empty() && curRun.actionType == ActionType.CLOSE) {
                    String value = this.contextStack.peek();
                    Token token = new Token(TokenizerUtils.getTokenType(value), value);
                    this.cursor += token.value.length();
                    this.updateState(token.type, token.value);
                    return token;
                }
                
                ArrayList<DelimRun> eqGroup = this.getRunEquivalenceGroup();
                this.balanced = eqGroup != null;

                /*
                 * if balanced, symbols can get processed as their proper token types such as italics and bold
                 * if not balanced, all symbols need to be processed as text
                 */
                if (this.balanced) {
                    DelimRun lastRun = eqGroup.get(eqGroup.size() - 1);
                    if (curRun.length == 3) {// ***1** 2*

                        if (lastRun.length == 3) {// ***1*** => **, *, 1***
                            tokenType = TokenType.BOLD;
                            tokenValue = startCharAsStr.repeat(2);

                            this.scheduledTokens.push(
                                new Token(TokenType.ITALICS, startCharAsStr)
                            );
                        } else {
                            tokenType = TokenizerUtils.getTokenType(lastRun.value);
                            tokenValue = lastRun.value;

                            DelimRun penultimateRun = eqGroup.get(eqGroup.size() - 2);
                            this.scheduledTokens.push(
                                new Token(TokenizerUtils.getTokenType(penultimateRun.value), penultimateRun.value)
                            );
                        }
                    } else {
                        tokenType = TokenizerUtils.getTokenType(curRun.value);
                        tokenValue = curRun.value;
                    }
                    this.cursor += tokenValue.length();
                }
                break;
            }
        }

        if (tokenType == null) {//no "special" token was found which means the upcoming characters must be a text token
            String text = this.getTextTokenValue();
            tokenType = TokenType.TEXT;
            tokenValue = text;
            this.cursor += text.length();
        }

        assert tokenValue != null;
        this.updateState(tokenType, tokenValue);

        return new Token(tokenType, tokenValue);
    }

    public static void main(String[] args) {
        // Tokenizer tokenizer = new Tokenizer("***innertext***    *ok*");
        // Tokenizer tokenizer = new Tokenizer("***innertext***  \n  *ok*");
        // Tokenizer tokenizer = new Tokenizer("***1 2** 3*");
        // Tokenizer tokenizer = new Tokenizer("*ok* **ok**");
        // Tokenizer tokenizer = new Tokenizer("***ok___");
        // Tokenizer tokenizer = new Tokenizer("*hi **how** are*");
        // Tokenizer tokenizer = new Tokenizer("*****hi how*** are**");
        // Tokenizer tokenizer = new Tokenizer("*******hi how** are*****");
        // Tokenizer tokenizer = new Tokenizer("***emph* in strong**");
        // Tokenizer tokenizer = new Tokenizer("***strong** in emph*");
        // Tokenizer tokenizer = new Tokenizer("**ok *ok ok* ok* ok*");
        // Tokenizer tokenizer = new Tokenizer("***1 *2* **3** ***4*** *5****");
        // Tokenizer tokenizer = new Tokenizer("**~~ok~~**");
        // Tokenizer tokenizer = new Tokenizer("\\***~~ok~~**");
        // Tokenizer tokenizer = new Tokenizer("**1* ~~sup~~");
        // Tokenizer tokenizer = new Tokenizer("**hi*");
        // Tokenizer tokenizer = new Tokenizer("**1 *2* *3* 4**");
        // Tokenizer tokenizer = new Tokenizer("*******1* 2*** 3***");
        // Tokenizer tokenizer = new Tokenizer("~~~ok~~~");
        // Tokenizer tokenizer = new Tokenizer("\\~~~ok~~\\~");
        // Tokenizer tokenizer = new Tokenizer("\\~hello");
        // Tokenizer tokenizer = new Tokenizer("\\~~hello");
        // Tokenizer tokenizer = new Tokenizer("\\*\\*1 \\*2\\* 3\\*\\*");
        // Tokenizer tokenizer = new Tokenizer("\\**_ok_**");
        // Tokenizer tokenizer = new Tokenizer("00*hey*00");
        // Tokenizer tokenizer = new Tokenizer("a**==*~~hey~~*==**a");
        // Tokenizer tokenizer = new Tokenizer("**==*~~hey~~*==**");
        // Tokenizer tokenizer = new Tokenizer("1*2**==3==**4*");
        // Tokenizer tokenizer = new Tokenizer("a*==hello==*");
        // Tokenizer tokenizer = new Tokenizer("=~=~==ok~=~=~==");
        // Tokenizer tokenizer = new Tokenizer("1***");
        Tokenizer tokenizer = new Tokenizer("0~~1~~2~~3~~4");
        // Tokenizer tokenizer = new Tokenizer("=~=~==ok~=~=~==");
        // Tokenizer tokenizer = new Tokenizer("1***2 ~~3~~");
        // Tokenizer tokenizer = new Tokenizer("**1 ****2 3**");
        // Tokenizer tokenizer = new Tokenizer("*******1* 2*** 3***");
        // Tokenizer tokenizer = new Tokenizer("=~=~==ok~=~=~==");
        // Tokenizer tokenizer = new Tokenizer("==~hey~==");
        // Tokenizer tokenizer = new Tokenizer("\\\\*emphasis*");
        // Tokenizer tokenizer = new Tokenizer("**1 *2* ***3*** 4**");
        // Tokenizer tokenizer = new Tokenizer("***1 ***2 *3*******");
        // Tokenizer tokenizer = new Tokenizer("*****1* 2** *3 **4  5*** 4**");
        // Tokenizer tokenizer = new Tokenizer("***1* 2* 3*");
        // Tokenizer tokenizer = new Tokenizer("***1** 2*");
        // Tokenizer tokenizer = new Tokenizer("*******1* 2*** 3***");
        // ArrayList<DelimRun> eqGroup = tokenizer.getRunEquivalenceGroup();
        // if (eqGroup != null) {
        //     System.out.println(Arrays.toString(eqGroup.stream().map(r -> r.value).toArray()));
        // }

        // System.out.println(tokenizer.string);
        // System.out.println(tokenizer.getCurDelimiterRun());
        // System.out.println(tokenizer.getNextDelimiterRun());

        Token token;
        while((token = tokenizer.getNextToken()) != null) {
            System.out.printf("%s: %s\n", token.type, token.value);
        }
        // System.out.println(tokenizer.getNextToken().value);
        // System.out.println(tokenizer.getNextToken().value);
        // System.out.println(tokenizer.getNextToken().value);
        // System.out.println(tokenizer.getNextToken().value);
        // System.out.println(tokenizer.getNextToken().value);
        // System.out.println(tokenizer.getNextToken().value);
        // System.out.println(tokenizer.getNextToken().value.length());
    }
}
