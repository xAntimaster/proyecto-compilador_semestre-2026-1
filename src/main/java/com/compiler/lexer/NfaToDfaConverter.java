package com.compiler.lexer;

import java.util.List;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * NfaToDfaConverter
 * -----------------
 * Converts a given NFA into an equivalent DFA
 * using the subset construction algorithm.
 */
public class NfaToDfaConverter {

    /**
     * Default constructor.
     * No initialization is required since all methods are static.
     */
    public NfaToDfaConverter() {
        // No initialization needed
    }
    
    /**
     * Converts an NFA into a DFA using the subset construction algorithm.
     *
     * @param nfa The NFA to be converted.
     * @param alphabet The input alphabet for the automaton.
     * @return A DFA equivalent to the given NFA.
     */
    public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
        // List of DFA states
        List<DfaState> dfaStates = new java.util.ArrayList<>();

        // Define token priority for final states (highest to lowest)
        String[] priority = {"KEYWORD", "IDENTIFIER", "NUMBER"};

        // Initial DFA state: epsilon-closure of the NFA start state
        Set<State> startClosure = epsilonClosure(java.util.Collections.singleton(nfa.startState));
        DfaState startDfaState = new DfaState(startClosure);
        dfaStates.add(startDfaState);

        // Queue for processing unmarked DFA states
        java.util.Queue<DfaState> queue = new java.util.LinkedList<>();
        queue.add(startDfaState);

        while (!queue.isEmpty()) {
            DfaState currentDfa = queue.poll();

            // Process each symbol in the alphabet
            for (char symbol : alphabet) {
                Set<State> moveResult = move(currentDfa.getNfaStates(), symbol);
                Set<State> closureResult = epsilonClosure(moveResult);

                if (closureResult.isEmpty()) continue;

                // Check if this set of NFA states already corresponds to an existing DFA state
                DfaState existing = findDfaState(dfaStates, closureResult);
                if (existing == null) {
                    DfaState newDfa = new DfaState(closureResult);
                    dfaStates.add(newDfa);
                    queue.add(newDfa);
                    existing = newDfa;
                }

                // Add DFA transition
                currentDfa.addTransition(symbol, existing);
            }
        }

        // Mark DFA states as final if any of their NFA states are final
        for (DfaState dfa : dfaStates) {
            boolean anyFinal = false;
            Token selectedToken = null;
            int highestPriority = priority.length;

            for (State nfaState : dfa.getNfaStates()) {
                if (nfaState.isFinal()) {
                    anyFinal = true;

                    // Optional token selection by priority
                    String type = nfaState.getTokenType(); // assumed available
                    int pr = java.util.Arrays.asList(priority).indexOf(type);
                    if (pr != -1 && pr < highestPriority) {
                        selectedToken = new Token(type, "", -1);
                        highestPriority = pr;
                    }
                }
            }

            //Always mark end if there is a final NFA, even if no token is selected.
            dfa.setFinal(anyFinal);
            if (anyFinal && selectedToken != null) {
                dfa.setToken(selectedToken);
            }
        }
        return new DFA(startDfaState, dfaStates);
    }

    /**
     * Computes the epsilon-closure of a set of NFA states.
     *
     * @param states The initial set of NFA states.
     * @return The epsilon-closure including all states reachable via epsilon transitions.
     */
    private static Set<State> epsilonClosure(Set<State> states) {
        Set<State> closure = new java.util.HashSet<>(states);
        java.util.Stack<State> stack = new java.util.Stack<>();
        for (State s : states) stack.push(s);

        while (!stack.isEmpty()) {
            State state = stack.pop();
            for (State target : state.getEpsilonTransitions()) {
                if (!closure.contains(target)) {
                    closure.add(target);
                    stack.push(target);
                }
            }
        }
        return closure;
    }

    /**
     * Computes the set of NFA states reachable from a given set of states
     * on a specific input symbol.
     *
     * @param states The current set of NFA states.
     * @param symbol The input symbol to process.
     * @return The set of reachable NFA states.
     */
    private static Set<State> move(Set<State> states, char symbol) {
        Set<State> result = new java.util.HashSet<>();
        for (State state : states) {
            for (State target : state.getTransitions(symbol)) {
                result.add(target);
            }
        }
        return result;
    }

    /**
     * Searches for an existing DFA state that corresponds to
     * the given set of NFA states.
     *
     * @param dfaStates The list of DFA states created so far.
     * @param targetNfaStates The set of NFA states to search for.
     * @return The matching DFA state if found, otherwise {@code null}.
     */
    private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
        for (DfaState dfa : dfaStates) {
            if (dfa.getNfaStates().equals(targetNfaStates)) return dfa;
        }
        return null;
    }
}