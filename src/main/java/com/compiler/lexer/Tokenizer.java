package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenizer
 * ----------
 * Uses a DFA to tokenize an input string.
 * Applies the "longest match" rule to produce a list of tokens
 * from the input string, skipping unrecognized characters.
 */
public class Tokenizer {

    private final DFA dfa;

    /**
     * Constructs a tokenizer using the given DFA.
     * @param dfa The DFA recognizing all token patterns.
     */
    public Tokenizer(DFA dfa) {
        this.dfa = dfa;
    }

    /**
     * Tokenizes the input string using the DFA.
     * @param input The input string to tokenize.
     * @return A list of tokens extracted from the input.
     * @throws RuntimeException If an invalid sequence is found.
     */
    public List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int position = 0;

        while (position < input.length()) {
            int lastMatchPos = -1;
            Token lastMatchedToken = null;
            DfaState currentState = dfa.startState;
            int currentPos = position;

            // Traverse the DFA as far as possible (longest match)
            while (currentPos < input.length()) {
                char symbol = input.charAt(currentPos);
                DfaState nextState = currentState.getTransition(symbol);

                if (nextState == null) break; // No further transition

                currentState = nextState;

                if (currentState.isFinal()) {
                    lastMatchPos = currentPos;
                    lastMatchedToken = currentState.getToken();
                }

                currentPos++;
            }

            if (lastMatchPos == -1) {
                throw new RuntimeException(
                    "Unexpected character at position " + position + ": '" + input.charAt(position) + "'"
                );
            }

            String lexeme = input.substring(position, lastMatchPos + 1);

            // Only add the token if it should not be ignored
            if (lastMatchedToken != null &&
                !lastMatchedToken.getType().equals("WHITESPACE") &&
                !lastMatchedToken.getType().equals("COMMENT")) {

                // Use pattern from lastMatchedToken if available
                String pattern = lastMatchedToken.getPattern();
                tokens.add(new Token(lastMatchedToken.getType(), lexeme, position, pattern));
            }

            // Move to the next position after the matched lexeme
            position = lastMatchPos + 1;
        }

        return tokens;
    }
}