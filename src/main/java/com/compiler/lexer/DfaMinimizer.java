package com.compiler.lexer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;


/**
 * Implements DFA minimization using the table-filling algorithm.
 */
/**
 * Utility class for minimizing DFAs using the table-filling algorithm.
 */
public class DfaMinimizer {
    
    /**
     * Default constructor for DfaMinimizer.
     */
        public DfaMinimizer() {
            // No initialization needed
        }

    /**
     * Minimizes a given DFA using the table-filling algorithm.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet The set of input symbols.
     * @return A minimized DFA equivalent to the original.
     */
    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        List<DfaState> states = originalDfa.allStates;

        // Table of distinguishability between state pairs
        Map<Pair, Boolean> table = new java.util.HashMap<>();

        // Step 1: Initialization (final vs non-final)
        for (int i = 0; i < states.size(); i++) {
            for (int j = i + 1; j < states.size(); j++) {
                DfaState s1 = states.get(i);
                DfaState s2 = states.get(j);
                boolean distinguishable = (s1.isFinal() != s2.isFinal());
                table.put(new Pair(s1, s2), distinguishable);
            }
        }

        // Step 2: Iterative marking
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

        // Step 3: Partitioning equivalent states
        List<Set<DfaState>> partitions = createPartitions(states, table);

        // Step 4: Build minimized DFA
        Map<DfaState, DfaState> representativeMap = new java.util.HashMap<>();
        List<DfaState> minimizedStates = new java.util.ArrayList<>();

        // Create a new state for each group (partition)
        for (Set<DfaState> group : partitions) {
            DfaState rep = new DfaState(new java.util.HashSet<>()); // New empty state

            // Mark end if any of the group is final
            boolean anyFinal = group.stream().anyMatch(DfaState::isFinal);
            rep.setFinal(anyFinal);

            // If final, assign the token of one of the states in the group
            if (anyFinal) {
                for (DfaState s : group) {
                    if (s.getToken() != null) {
                        rep.setToken(s.getToken());
                        break;
                    }
                }
            }

            minimizedStates.add(rep);

            // Map all old states of this group to the new representative
            for (DfaState old : group) {
                representativeMap.put(old, rep);
            }
        }

        // Step 5: Rebuild transitions
        for (Set<DfaState> group : partitions) {
            DfaState rep = representativeMap.get(group.iterator().next());
            for (DfaState old : group) {
                for (java.util.Map.Entry<Character, DfaState> entry : old.getTransitions().entrySet()) {
                    rep.addTransition(entry.getKey(), representativeMap.get(entry.getValue()));
                }
            }
        }

        // Step 6: Identify new start state
        DfaState minimizedStart = representativeMap.get(originalDfa.startState);

        return new DFA(minimizedStart, minimizedStates);

    }

    /**
     * Groups equivalent states into partitions using union-find.
     *
     * @param allStates List of all DFA states.
     * @param table Table indicating which pairs are distinguishable.
     * @return List of partitions, each containing equivalent states.
     */
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

    /**
     * Finds the root parent of a state in the union-find structure.
     * Implements path compression for efficiency.
     *
     * @param parent Parent map.
     * @param state State to find.
     * @return Root parent of the state.
     */
    private static DfaState find(Map<DfaState, DfaState> parent, DfaState s) {
        if (parent.get(s) == s) return s;
        DfaState root = find(parent, parent.get(s));
        parent.put(s, root);
        return root;
    }

    /**
     * Unites two states in the union-find structure.
     *
     * @param parent Parent map.
     * @param s1 First state.
     * @param s2 Second state.
     */
    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        DfaState root1 = find(parent, s1);
        DfaState root2 = find(parent, s2);
        if (root1 != root2) {
            parent.put(root1, root2);
        }
    }

    /**
     * Helper class to represent a pair of DFA states in canonical order.
     * Used for table indexing and comparison.
     */
    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        /**
         * Constructs a pair in canonical order (lowest id first).
         * @param s1 First state.
         * @param s2 Second state.
         */
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