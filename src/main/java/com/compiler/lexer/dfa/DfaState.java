package com.compiler.lexer.dfa;

import java.util.Map;
import java.util.Set;

import com.compiler.lexer.Token;
import com.compiler.lexer.nfa.State;

/**
 * DfaState
 * --------
 * Represents a single state in a Deterministic Finite Automaton (DFA).
 * Each DFA state corresponds to a set of states from the original NFA.
 * Provides methods for managing transitions, checking finality,
 * and equality based on NFA state sets.
 */
public class DfaState {
    /**
     * Unique identifier for this DFA state.
     */
    public final int id;

    /**
     * The set of NFA states this DFA state represents.
     */
    public final Set<State> nfaStates;

    /**
     * Indicates whether this DFA state is a final (accepting) state.
     */
    public boolean isFinal;

    /**
     * Map of input symbols to destination DFA states (transitions).
     */
    public final Map<Character, DfaState> transitions;

    /**
     * Counter to assign unique IDs to DFA states.
     */
    private static int nextId = 0;

    /**
     * The type of token recognized by this state if it is final.
     * Null if the state is not final.
     */
    private Token token;

    /**
     * Constructs a new DFA state.
     * @param nfaStates The set of NFA states that this DFA state represents.
     */
    public DfaState(Set<State> nfaStates) {
        this.id = nextId++;
        this.nfaStates = nfaStates;
        this.isFinal = false;
        this.transitions = new java.util.HashMap<>();
        this.token = null;
    }

    /**
     * Adds a transition from this state to another on a given symbol.
     * @param symbol The input symbol for the transition.
     * @param toState The destination DFA state.
     */
    public void addTransition(Character symbol, DfaState toState) {
        transitions.put(symbol, toState);
    }

    /**
     * Returns all transitions from this state.
     * @return Map of input symbols to destination DFA states.
     */
    public Map<Character, DfaState> getTransitions() {
        return transitions;
    }

    /**
     * Returns the set of NFA states this DFA state represents.
     * @return The set of NFA states.
     */
    public Set<State> getNfaStates() {
        return nfaStates;
    }

    /**
     * Sets the finality of the DFA state.
     * @param isFinal True if this state is a final state, false otherwise.
     */
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Checks if the DFA state is final.
     * @return True if this state is a final state, false otherwise.
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Gets the transition for a given input symbol.
     * @param symbol The input symbol for the transition.
     * @return The destination DFA state for the transition,
     * or null if there is no transition for the given symbol.
     */
    public DfaState getTransition(char symbol) {
        return transitions.get(symbol);
    }

    /**
     * Sets the token for this state and marks it as final.
     * Should only be called for final states.
     */
    public void setToken(Token newToken) {
        this.isFinal = true; // Ensure the state is final
        if (this.token == null || newToken.getPriority() < this.token.getPriority()) {
            // Lower priority number means higher precedence (e.g., reserved words)
            this.token = newToken;
        }
    }

    /**
     * Returns the token associated with this state.
     * Returns null if the state is not final.
     */
    public Token getToken() {
        return token;
    }

    /**
     * Two DfaStates are considered equal if they represent
     * the same set of NFA states.
     * @param obj The object to compare.
     * @return True if the states are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DfaState)) return false;
        DfaState other = (DfaState) obj;
        return nfaStates.equals(other.nfaStates);
    }

    /**
     * The hash code is based on the set of NFA states.
     * @return The hash code for this DFA state.
     */
    @Override
    public int hashCode() {
        return nfaStates.hashCode();
    }

    /**
     * Returns a string representation of the DFA state,
     * including its id and finality.
     * @return String representation of the state.
     */
    @Override
    public String toString() {
        return "DfaState{id=" + id + ", isFinal=" + isFinal + ", token=" + token + "}";
    }
}