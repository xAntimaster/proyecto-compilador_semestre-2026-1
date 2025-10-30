package com.compiler.lexer.nfa;

import java.util.List;

/**
 * Represents a state in a Non-deterministic Finite Automaton (NFA).
 * Each state has a unique identifier, a list of transitions to other states,
 * and a flag indicating whether it is a final (accepting) state.
 */
public class State {
    public static int nextId = 0;

    /**
     * Unique identifier for this state.
     */
    public final int id;

    /**
     * List of transitions from this state to other states.
     */
    public List<Transition> transitions;

    /**
     * Indicates if this state is a final (accepting) state.
     */
    public boolean isFinal;

    /** 
     * Type of token recognized by this state (if final). 
     */
    private String tokenType;

    /**
     * Constructs a new state with a unique identifier and no transitions.
     * The state is not final by default.
     */
    public State() {
        this.id = nextId++;
        this.transitions = new java.util.LinkedList<>();
        this.isFinal = false;
        this.tokenType = null;
    }

    /**
     * Checks if this state is a final (accepting) state.
     * @return true if this state is final, false otherwise
     */
    public boolean isFinal() {
        return this.isFinal;
    }

    /** Gets the token type of this state (null if not final). 
     * 
     * @return 
     */
    public String getTokenType() { 
        return tokenType; 
    }

    /** 
     * Sets the token type for this state (only meaningful if final).
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * Returns the states reachable from this state via epsilon transitions (symbol
     * == null).
     *
     * @return a list of states reachable by epsilon transitions
     */
    public List<State> getEpsilonTransitions() {
        List<State> epsilonStates = new java.util.LinkedList<>();
        for (Transition t : transitions) {
            if (t.symbol == null) {
                epsilonStates.add(t.toState);
            }
        }
        return epsilonStates;
    }

    /**
     * Returns the states reachable from this state via a transition with the given
     * symbol.
     *
     * @param symbol the symbol for the transition
     * @return a list of states reachable by the given symbol
     */
    public List<State> getTransitions(char symbol) {
        List<State> symbolStates = new java.util.LinkedList<>();
        for (Transition t : transitions) {
            if (t.symbol != null && t.symbol == symbol) {
                symbolStates.add(t.toState);
            }
        }
        return symbolStates;
    }
}