package org.vaadin.editor.tokenizer;
// package org;
import org.junit.Test;
import org.junit.Assert;

public class TokenizerTests {

    /**
     * main testing method used to assert that the tokenizer's output matches the given expected tokens
     */
    public void assertTokenizedString(String stringToTokenize, Token[] expectedTokens) {
        Tokenizer tokenizer = new Tokenizer(stringToTokenize);
        Token receivedToken;
        for (int i = 0; i < expectedTokens.length; i++) {
            receivedToken = tokenizer.getNextToken();
            Token expectedToken = expectedTokens[i];
            Assert.assertEquals(receivedToken.type, expectedToken.type);
            Assert.assertEquals(receivedToken.value, expectedToken.value);
        }
    }

    public void assertTokenizedString(String stringToTokenize, String[] expectedTokenValues) {
        Tokenizer tokenizer = new Tokenizer(stringToTokenize);
        Token receivedToken;
        for (int i = 0; i < expectedTokenValues.length; i++) {
            receivedToken = tokenizer.getNextToken();
            String expectedTokenValue = expectedTokenValues[i];
            Assert.assertEquals(receivedToken.type, TokenizerUtils.getTokenType(expectedTokenValue));
            Assert.assertEquals(receivedToken.value, expectedTokenValue);
        }
    }

    @Test
    public void normalText() {
        Token[] expectedTokens = {
            new Token(TokenType.TEXT, "just some text")
        };
        this.assertTokenizedString("just some text", expectedTokens);

        Token[] expectedTokens1 = {
            new Token(TokenType.TEXT, "~just some text")
        };
        this.assertTokenizedString("~just some text", expectedTokens1);

        Token[] expectedTokens2 = {
            new Token(TokenType.TEXT, "~just some text~")
        };
        this.assertTokenizedString("~just some text~", expectedTokens2);
    }

    @Test
    public void strikethrough() {

        Token[] expectedTokens = {
            new Token(TokenType.STRIKETHROUGH, "~~"),
            new Token(TokenType.TEXT, "hello"),
            new Token(TokenType.STRIKETHROUGH, "~~"),
        };

        this.assertTokenizedString("~~hello~~", expectedTokens);
    }

    @Test
    public void highlight() {

        Token[] expectedTokens = {
            new Token(TokenType.HIGHLIGHT, "=="),
            new Token(TokenType.TEXT, "hello"),
            new Token(TokenType.HIGHLIGHT, "=="),
        };

        this.assertTokenizedString("==hello==", expectedTokens);
    }

    @Test
    public void italics() {
        //asterisks

        Token[] expectedTokens = {
            new Token(TokenType.ITALICS, "*"),
            new Token(TokenType.TEXT, "hello"),
            new Token(TokenType.ITALICS, "*"),
        };

        this.assertTokenizedString("*hello*", expectedTokens);
    }

    @Test
    public void bold() {
        //asterisks

        Token[] expectedTokens = {
            new Token(TokenType.BOLD, "**"),
            new Token(TokenType.TEXT, "hello"),
            new Token(TokenType.BOLD, "**"),
        };

        this.assertTokenizedString("**hello**", expectedTokens);

        //underlines

        Token[] expectedTokens1 = {
            new Token(TokenType.BOLD, "__"),
            new Token(TokenType.TEXT, "hello"),
            new Token(TokenType.BOLD, "__"),
        };

        this.assertTokenizedString("__hello__", expectedTokens1);
    }

    @Test
    public void nestedBoldAndItalics() {

        Token[] expectedTokens = {
            new Token(TokenType.BOLD, "**"),
            new Token(TokenType.ITALICS, "*"),
            new Token(TokenType.TEXT, "hello"),
            new Token(TokenType.ITALICS, "*"),
            new Token(TokenType.BOLD, "**"),
        };

        this.assertTokenizedString("***hello***", expectedTokens);

        String[] expectedTokenValues = {"_", "**", "ok", "**", "_"};
        this.assertTokenizedString("_**ok**_", expectedTokenValues);
    }

    @Test
    public void escape() {
        String[] expectedTokenValues = {"\\s"};
        this.assertTokenizedString("\\s", expectedTokenValues);

        String[] expectedTokenValues1 = {"\\"};
        this.assertTokenizedString("\\", expectedTokenValues1);

        Token[] expectedTokens = {
            new Token(TokenType.TEXT, "*"),
        };
        this.assertTokenizedString("\\*", expectedTokens);

        Token[] expectedTokens1 = {
            new Token(TokenType.TEXT, "*"),
            new Token(TokenType.TEXT, "*_ok_**"),
        };
        this.assertTokenizedString("\\**_ok_**", expectedTokens1);

        Token[] expectedTokens2 = {
            new Token(TokenType.TEXT, "*"),
            new Token(TokenType.BOLD, "**"),
            new Token(TokenType.STRIKETHROUGH, "~~"),
            new Token(TokenType.TEXT, "ok"),
            new Token(TokenType.STRIKETHROUGH, "~~"),
            new Token(TokenType.BOLD, "**"),
        };
        this.assertTokenizedString("\\***~~ok~~**", expectedTokens2);

        Token[] expectedTokens3 = {
            new Token(TokenType.TEXT, "*"),
            new Token(TokenType.TEXT, "*"),
            new Token(TokenType.TEXT, "1 "),
            new Token(TokenType.TEXT, "*"),
            new Token(TokenType.TEXT, "2"),
            new Token(TokenType.TEXT, "*"),
            new Token(TokenType.TEXT, " 3"),
            new Token(TokenType.TEXT, "*"),
            new Token(TokenType.TEXT, "*"),
        };
        this.assertTokenizedString("\\*\\*1 \\*2\\* 3\\*\\*", expectedTokens3);

        String[] expectedTokenValues2 = {"**", "1 ", "\\", "*", "2", "*", " 3", "**"};
        this.assertTokenizedString("**1 \\\\*2* 3**", expectedTokenValues2);

        String[] expectedTokenValues3 = {"\\", "*", "emphasis", "*"};
        this.assertTokenizedString("\\\\*emphasis*", expectedTokenValues3);
    }

    @Test
    public void miscellaneous() {
        String[] expectedTokenValues = {"**", "*", "1", "*", "**", " ", "*", "2", "*"};
        this.assertTokenizedString("***1*** *2*", expectedTokenValues);
        
        String[] expectedTokenValues1 = {"**", "1", "**", " ", "*", "2", "*", " ", "**", "*", "3", "*", "**"};
        this.assertTokenizedString("**1** *2* ***3***", expectedTokenValues1);

        String[] expectedTokenValues2 = {"**1*"};
        this.assertTokenizedString("**1*", expectedTokenValues2);

        String[] expectedTokenValues3 = {"**", "1 ", "*", "2", "*", " ", "*", "3", "*", " 4", "**"};
        this.assertTokenizedString("**1 *2* *3* 4**", expectedTokenValues3);

        String[] expectedTokenValues4 = {"**1* ~~sup~~"};
        this.assertTokenizedString("**1* ~~sup~~", expectedTokenValues4);

        String[] expectedTokenValues5 = {"*1_ **2**"};
        this.assertTokenizedString("*1_ **2**", expectedTokenValues5);

        String[] expectedTokenValues6 = {"***ok___"};
        this.assertTokenizedString("***ok___", expectedTokenValues6);

        String[] expectedTokenValues7 = {"*******1* 2*** 3***"};
        this.assertTokenizedString("*******1* 2*** 3***", expectedTokenValues7);

        String[] expectedTokenValues8 = {"**", "*", "==", "~~", "hey", "~~", "==", "*", "**"};
        this.assertTokenizedString("***==~~hey~~==***", expectedTokenValues8);

        String[] expectedTokenValues9 = {"=~=~", "==", "ok~=~=~", "=="};
        this.assertTokenizedString("=~=~==ok~=~=~==", expectedTokenValues9);

        String[] expectedTokenValues10 = {"0", "~~", "1", "~~", "2", "~~", "3", "~~", "4"};
        this.assertTokenizedString("0~~1~~2~~3~~4", expectedTokenValues10);
    }

    /**
     * Doesn't actually assert any specific values, just makes sure all tokens can be extracted from the text without crashing
     */
    @Test
    public void crash() {
        Tokenizer[] tokenizers = {
            new Tokenizer("***innertext***    *ok*"),
            new Tokenizer("***innertext***  \n  *ok*"),
            new Tokenizer("***1 2** 3*"),
            new Tokenizer("*ok* **ok**"),
            new Tokenizer("***ok___"),
            new Tokenizer("*hi **how** are*"),
            new Tokenizer("*****hi how*** are**"),
            new Tokenizer("*******hi how** are*****"),
            new Tokenizer("***emph* in strong**"),
            new Tokenizer("***strong** in emph*"),
            new Tokenizer("**ok *ok ok* ok* ok*"),
            new Tokenizer("***1 *2* **3** ***4*** *5****"),
            new Tokenizer("**~~ok~~**"),
            new Tokenizer("\\***~~ok~~**"),
            new Tokenizer("**1* ~~sup~~"),
            new Tokenizer("**hi*"),
            new Tokenizer("**1 *2* *3* 4**"),
            new Tokenizer("*******1* 2*** 3***"),
            new Tokenizer("~~~ok~~~"),
            new Tokenizer("\\~~~ok~~\\~"),
            new Tokenizer("\\~hello"),
            new Tokenizer("\\~~hello"),
            new Tokenizer("\\*\\*1 \\*2\\* 3\\*\\*"),
            new Tokenizer("\\**_ok_**"),
            new Tokenizer("*******1* 2*** 3***"),
            new Tokenizer("**1 ****2 3**"),
            new Tokenizer("*******1* 2*** 3***"),
            new Tokenizer("=~=~==ok~=~=~=="),
            new Tokenizer("==~hey~=="),
            new Tokenizer("\\\\*emphasis*"),
            new Tokenizer("**1 *2* ***3*** 4**"),
            new Tokenizer("***1 ***2 *3*******"),
            new Tokenizer("*****1* 2** *3 **4  5*** 4**"),
            new Tokenizer("***1* 2* 3*"),
            new Tokenizer("***1** 2*"),
            new Tokenizer("*******1* 2*** 3***"),
        };

        for (Tokenizer tokenizer : tokenizers) {
            Token token;
            while((token = tokenizer.getNextToken()) != null) {
                // System.out.printf("%s: %s\n", token.type, token.value);
            }
        }
    }
}
