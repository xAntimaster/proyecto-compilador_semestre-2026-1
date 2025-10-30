package com.compiler.lexer.regex;

import java.util.Stack;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;

/**
 * RegexParser
 * -----------
 * Parses regular expressions and constructs NFAs using Thompson's construction.
 * Supports the operators: concatenation (·), union (|), Kleene star (*),
 * optional (?), and plus (+).
 */
public class RegexParser {

    private final String tokenType;

    public RegexParser() {
        this.tokenType = null; // default tokenType
    }

    /**
     * Constructor for RegexParser with token type.
     *
     * @param tokenType The type of token this regex represents.
     */
    public RegexParser(String tokenType) {
        this.tokenType = tokenType;
    }

    public NFA parse(String infixRegex) {
        // Convert infix regex to postfix using Shunting Yard algorithm
        String postfix = ShuntingYard.toPostfix(infixRegex);
        NFA nfa = buildNfaFromPostfix(postfix);

        // Assign tokenType to the final state
        nfa.endState.isFinal = true;
        nfa.endState.setTokenType(tokenType);

        return nfa;
    }

    private NFA buildNfaFromPostfix(String postfixRegex) {
        Stack<NFA> stack = new Stack<>();

        for (int i = 0; i < postfixRegex.length(); i++) {
            char c = postfixRegex.charAt(i);

            if (isOperand(c)) {
                stack.push(createNfaForCharacter(c));
            } else {
                switch (c) {
                    case '·': handleConcatenation(stack); break;
                    case '|': handleUnion(stack); break;
                    case '*': handleKleeneStar(stack); break;
                    case '?': handleOptional(stack); break;
                    case '+': handlePlus(stack); break;
                    default: throw new IllegalArgumentException("Invalid operator: " + c);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Invalid postfix regex, stack size != 1");
        }

        return stack.pop();
    }

    /**
     * Handles the '?' operator (zero or one occurrence)
     * Fixed to properly support concatenation.
     */
    private void handleOptional(Stack<NFA> stack) {
        NFA nfa = stack.pop();

        State start = new State();

        // Epsilon from new start to NFA start (1 occurrence)
        start.transitions.add(new Transition(null, nfa.startState));
        // Epsilon from new start directly to NFA end (0 occurrence)
        start.transitions.add(new Transition(null, nfa.endState));

        // Keep original NFA end as final
        nfa.endState.isFinal = true;
        nfa.endState.setTokenType(tokenType);

        // Push NFA with new start, original end
        stack.push(new NFA(start, nfa.endState));
    }

    /**
     * Handles the '+' operator (one or more occurrences)
     */
    private void handlePlus(Stack<NFA> stack) {
        NFA nfa = stack.pop();

        State start = new State();
        State end = new State();

        start.transitions.add(new Transition(null, nfa.startState));
        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        nfa.endState.transitions.add(new Transition(null, end));

        end.isFinal = true;
        end.setTokenType(tokenType);

        stack.push(new NFA(start, end));
    }

    private NFA createNfaForCharacter(char c) {
        State start = new State();
        State end = new State();

        start.transitions.add(new Transition(c, end));
        end.isFinal = true;
        end.setTokenType(tokenType);

        return new NFA(start, end);
    }

    private void handleConcatenation(Stack<NFA> stack) {
        NFA nfa2 = stack.pop();
        NFA nfa1 = stack.pop();

        nfa1.endState.isFinal = false;
        nfa1.endState.setTokenType(null);
        nfa1.endState.transitions.add(new Transition(null, nfa2.startState));

        stack.push(new NFA(nfa1.startState, nfa2.endState));
    }

    private void handleUnion(Stack<NFA> stack) {
        NFA nfa2 = stack.pop();
        NFA nfa1 = stack.pop();

        State start = new State();
        State end = new State();

        start.transitions.add(new Transition(null, nfa1.startState));
        start.transitions.add(new Transition(null, nfa2.startState));

        nfa1.endState.isFinal = false;
        nfa1.endState.setTokenType(null);
        nfa2.endState.isFinal = false;
        nfa2.endState.setTokenType(null);

        nfa1.endState.transitions.add(new Transition(null, end));
        nfa2.endState.transitions.add(new Transition(null, end));

        end.isFinal = true;
        end.setTokenType(tokenType);

        stack.push(new NFA(start, end));
    }

    private void handleKleeneStar(Stack<NFA> stack) {
        NFA nfa = stack.pop();

        State start = new State();
        State end = new State();

        start.transitions.add(new Transition(null, nfa.startState));
        start.transitions.add(new Transition(null, end));

        nfa.endState.transitions.add(new Transition(null, nfa.startState));
        nfa.endState.transitions.add(new Transition(null, end));

        nfa.endState.isFinal = false;
        nfa.endState.setTokenType(null);

        end.isFinal = true;
        end.setTokenType(tokenType);

        stack.push(new NFA(start, end));
    }

    private boolean isOperand(char c) {
        return !(c == '·' || c == '|' || c == '*' || c == '?' || c == '+' || c == '(' || c == ')');
    }
}