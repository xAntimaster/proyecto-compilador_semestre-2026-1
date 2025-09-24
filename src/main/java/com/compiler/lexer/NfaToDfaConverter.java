package com.compiler.lexer;

import java.util.List;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

public class NfaToDfaConverter {

    public NfaToDfaConverter() {
        // No initialization needed
    }
    
    public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
        // Lista de estados del DFA
        List<DfaState> dfaStates = new java.util.ArrayList<>();

        // Prioridades de tipos de token (si aplica)
        String[] priority = {"KEYWORD", "IDENTIFIER", "NUMBER"};

        // Estado inicial del DFA: ε-closure del estado inicial del NFA
        Set<State> startClosure = epsilonClosure(java.util.Collections.singleton(nfa.startState));
        DfaState startDfaState = new DfaState(startClosure);
        dfaStates.add(startDfaState);

        // Cola de trabajo
        java.util.Queue<DfaState> queue = new java.util.LinkedList<>();
        queue.add(startDfaState);

        while (!queue.isEmpty()) {
            DfaState currentDfa = queue.poll();

            // Transiciones por cada símbolo del alfabeto
            for (char symbol : alphabet) {
                Set<State> moveResult = move(currentDfa.getNfaStates(), symbol);
                Set<State> closureResult = epsilonClosure(moveResult);

                if (closureResult.isEmpty()) continue;

                // Reusar o crear estado DFA
                DfaState existing = findDfaState(dfaStates, closureResult);
                if (existing == null) {
                    DfaState newDfa = new DfaState(closureResult);
                    dfaStates.add(newDfa);
                    queue.add(newDfa);
                    existing = newDfa;
                }

                // Agregar transición determinista
                currentDfa.addTransition(symbol, existing);
            }
        }

        // --- MARCADO DE FINALES Y TOKEN ---
        for (DfaState dfa : dfaStates) {
            boolean anyFinal = false;
            Token selectedToken = null;
            int highestPriority = priority.length;

            for (State nfaState : dfa.getNfaStates()) {
                if (nfaState.isFinal()) {
                    anyFinal = true;

                    // Selección opcional de token por prioridad
                    String type = nfaState.getTokenType(); // se asume disponible
                    int pr = java.util.Arrays.asList(priority).indexOf(type);
                    if (pr != -1 && pr < highestPriority) {
                        selectedToken = new Token(type, "", -1);
                        highestPriority = pr;
                    }
                }
            }

            // 🔧 Cambio clave: marcar final SIEMPRE si hay algún NFA final,
            //                 aunque no se haya seleccionado token.
            dfa.setFinal(anyFinal);
            if (anyFinal && selectedToken != null) {
                dfa.setToken(selectedToken);
            }
        }

        return new DFA(startDfaState, dfaStates);
    }

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

    private static Set<State> move(Set<State> states, char symbol) {
        Set<State> result = new java.util.HashSet<>();
        for (State state : states) {
            for (State target : state.getTransitions(symbol)) {
                result.add(target);
            }
        }
        return result;
    }

    private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
        for (DfaState dfa : dfaStates) {
            if (dfa.getNfaStates().equals(targetNfaStates)) return dfa;
        }
        return null;
    }
}
