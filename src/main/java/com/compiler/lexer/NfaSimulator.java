package com.compiler.lexer;

import java.util.Set;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * Simulator for running input strings on an NFA.
 */
public class NfaSimulator {

    public NfaSimulator() {
        // No implementation needed
    }

    public boolean simulate(NFA nfa, String input) {
        Set<State> currentStates = new java.util.HashSet<>();
        addEpsilonClosure(nfa.startState, currentStates);

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Set<State> nextStates = new java.util.HashSet<>();

            for (State state : currentStates) {
                for (State dest : state.getTransitions(c)) {
                    addEpsilonClosure(dest, nextStates);
                }
            }

            currentStates = nextStates;
        }

        for (State state : currentStates) {
            // Only accept if the final state is marked as such
            if (state.isFinal()) {
                return true;
            }
        }

        return false;
    }

    private void addEpsilonClosure(State start, Set<State> closureSet) {
        if (!closureSet.contains(start)) {
            closureSet.add(start);
            for (State next : start.getEpsilonTransitions()) {
                addEpsilonClosure(next, closureSet);
            }
        }
    }
}