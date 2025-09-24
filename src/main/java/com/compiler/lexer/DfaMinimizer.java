package com.compiler.lexer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;

/**
 * Utility class for minimizing DFAs using the table-filling algorithm.
 */
public class DfaMinimizer {
    public DfaMinimizer() {
        // No initialization needed
    }

    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        List<DfaState> states = originalDfa.allStates;

        // Tabla de distinguibilidad
        Map<Pair, Boolean> table = new java.util.HashMap<>();

        // Paso 1: Inicialización (final vs no-final)
        for (int i = 0; i < states.size(); i++) {
            for (int j = i + 1; j < states.size(); j++) {
                DfaState s1 = states.get(i);
                DfaState s2 = states.get(j);
                boolean distinguishable = (s1.isFinal() != s2.isFinal());
                table.put(new Pair(s1, s2), distinguishable);
            }
        }

        // Paso 2: Marcado iterativo
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < states.size(); i++) {
                for (int j = i + 1; j < states.size(); j++) {
                    Pair pair = new Pair(states.get(i), states.get(j));
                    if (Boolean.TRUE.equals(table.get(pair))) continue;

                    for (char symbol : alphabet) {
                        DfaState t1 = states.get(i).getTransition(symbol);
                        DfaState t2 = states.get(j).getTransition(symbol);

                        if (t1 == null && t2 == null) continue;
                        if (t1 == null || t2 == null || Boolean.TRUE.equals(table.get(new Pair(t1, t2)))) {
                            table.put(pair, true);
                            changed = true;
                            break;
                        }
                    }
                }
            }
        } while (changed);

        // Paso 3: Particionar estados equivalentes
        List<Set<DfaState>> partitions = createPartitions(states, table);

        // Paso 4: Construir estados del DFA minimizado
        Map<DfaState, DfaState> representativeMap = new java.util.HashMap<>();
        List<DfaState> minimizedStates = new java.util.ArrayList<>();

        for (Set<DfaState> group : partitions) {
            DfaState rep = group.iterator().next();
            DfaState newState = new DfaState(rep.getNfaStates());

            boolean isFinal = false;
            com.compiler.lexer.Token token = null;
            int priority = Integer.MAX_VALUE;

            for (DfaState s : group) {
                if (s.isFinal()) {
                    isFinal = true;
                    com.compiler.lexer.Token t = s.getToken();
                    if (t != null && t.getPriority() < priority) {
                        token = t;
                        priority = t.getPriority();
                    }
                }
                representativeMap.put(s, newState);
            }
            newState.setFinal(isFinal);
            if (isFinal && token != null) newState.setToken(token);

            minimizedStates.add(newState);
        }

        // Paso 5: Reconstruir transiciones del DFA minimizado
        for (Set<DfaState> group : partitions) {
            DfaState newState = representativeMap.get(group.iterator().next());
            Map<Character, DfaState> transitions = new java.util.HashMap<>();

            // 🔧 Cambio clave: copiar transiciones REALES de los estados del grupo,
            // en lugar de depender únicamente del 'alphabet'
            for (DfaState s : group) {
                for (java.util.Map.Entry<Character, DfaState> e : s.getTransitions().entrySet()) {
                    Character symbol = e.getKey();
                    DfaState target = e.getValue();
                    transitions.putIfAbsent(symbol, representativeMap.get(target));
                }
            }

            // Agregar transiciones al nuevo estado
            for (Map.Entry<Character, DfaState> entry : transitions.entrySet()) {
                newState.addTransition(entry.getKey(), entry.getValue());
            }
        }

        // Paso 6: Identificar el nuevo estado inicial
        DfaState minimizedStart = representativeMap.get(originalDfa.startState);

        return new DFA(minimizedStart, minimizedStates);
    }

    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
        Map<DfaState, DfaState> parent = new java.util.HashMap<>();
        for (DfaState s : allStates) {
            parent.put(s, s);
        }

        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                Pair pair = new Pair(allStates.get(i), allStates.get(j));
                if (!Boolean.TRUE.equals(table.get(pair))) union(parent, allStates.get(i), allStates.get(j));
            }
        }

        Map<DfaState, Set<DfaState>> groups = new java.util.HashMap<>();
        for (DfaState s : allStates) {
            DfaState root = find(parent, s);
            groups.computeIfAbsent(root, k -> new java.util.HashSet<>()).add(s);
        }

        return new java.util.ArrayList<>(groups.values());
    }

    private static DfaState find(Map<DfaState, DfaState> parent, DfaState s) {
        if (parent.get(s) == s) return s;
        DfaState root = find(parent, parent.get(s));
        parent.put(s, root);
        return root;
    }

    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        DfaState root1 = find(parent, s1);
        DfaState root2 = find(parent, s2);
        if (root1 != root2) {
            parent.put(root1, root2);
        }
    }

    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        Pair(DfaState a, DfaState b) {
            if (a.id <= b.id) {
                s1 = a; s2 = b;
            } else {
                s1 = b; s2 = a;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair other = (Pair) o;
            return s1.equals(other.s1) && s2.equals(other.s2);
        }

        @Override
        public int hashCode() {
            return 31 * s1.hashCode() + s2.hashCode();
        }
    }
}