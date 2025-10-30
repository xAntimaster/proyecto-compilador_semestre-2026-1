package com.compiler.lexer;

/**
 * Token
 * -----
 * Represents a lexical token produced by the Tokenizer.
 * Each token has:
 *   - A type (e.g., IDENTIFIER, NUMBER, KEYWORD, etc.)
 *   - The lexeme (the exact substring from the input)
 *   - The position (index in the input string for error reporting)
 *   - Optional pattern info (useful for debugging or optional parts)
 */
public class Token {
    private final String type;
    private final String lexeme;
    private final int position; // index in the input string
    private final String pattern; // optional: pattern/regex that generated this token

    /**
     * Constructs a new Token with type, lexeme, and position.
     *
     * @param type     The type of the token (e.g., "IDENTIFIER", "NUMBER").
     * @param lexeme   The exact substring matched.
     * @param position The starting index of the token in the input string.
     */
    public Token(String type, String lexeme, int position) {
        this(type, lexeme, position, null);
    }

    /**
     * Constructs a new Token with type, lexeme, position, and optional pattern.
     *
     * @param type     The type of the token.
     * @param lexeme   The exact substring matched.
     * @param position The starting index in the input string.
     * @param pattern  Optional pattern/regex that produced this token.
     */
    public Token(String type, String lexeme, int position, String pattern) {
        this.type = type;
        this.lexeme = lexeme;
        this.position = position;
        this.pattern = pattern;
    }

    /**
     * Returns the type of the token.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the lexeme (the matched substring).
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * Returns the position (starting index in the input string).
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns the optional pattern/regex that generated this token.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Returns the priority of the token for DFA state resolution.
     * Lower number = higher priority (KEYWORD > IDENTIFIER > NUMBER).
     */
    public int getPriority() {
        switch (type) {
            case "KEYWORD": return 0;
            case "IDENTIFIER": return 1;
            case "NUMBER": return 2;
            default: return Integer.MAX_VALUE; // other types have lower priority
        }
    }

    @Override
    public String toString() {
        return "Token(type=" + type + ", lexeme=\"" + lexeme + "\", position=" + position +
               (pattern != null ? ", pattern=\"" + pattern + "\"" : "") + ")";
    }
}