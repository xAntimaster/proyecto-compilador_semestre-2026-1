package com.compiler.lexer.dfa;

import java.util.List;

/**
 * DFA
 * ---
 * Represents a complete Deterministic Finite Automaton (DFA).
 * Contains the start state and a list of all states in the automaton.
 */
public class DFA {
    /**
     * The starting state of the DFA.
     */
    public final DfaState startState;

    /**
     * A list of all states in the DFA.
     */
    public final List<DfaState> allStates;

    /**
     * Constructs a new DFA with a given start state and list of all states.
     *
     * @param startState The starting state of the DFA.
     * @param allStates  A list of all DFA states.
     */
    public DFA(DfaState startState, List<DfaState> allStates) {
        this.startState = startState;
        this.allStates = allStates;
    }

    /**
     * Returns all states of the DFA.
     * @return List of DFA states.
     */
    public List<DfaState> getAllStates() {
        return allStates;
    }

    /**
     * Finds a DFA state by its ID.
     * Useful for debugging or inspection.
     * 
     * @param id The ID of the DFA state.
     * @return The DfaState with the given ID, or null if not found.
     */
    public DfaState getStateById(int id) {
        for (DfaState state : allStates) {
            if (state.id == id) {
                return state;
            }
        }
        return null;
    }

    /**
     * Returns a string representation of the DFA,
     * showing the start state and all states.
     */
    @Override
    public String toString() {
        return "DFA{startState=" + startState + ", totalStates=" + allStates.size() + "}";
    }
}