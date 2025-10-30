package com.compiler.parser.syntax;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * Main task of Practice 5.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<Symbol, Set<Symbol>> followSets;

    // Local definitions of special symbols
    private final Symbol EPSILON = new Symbol("ε", SymbolType.TERMINAL);
    private final Symbol EOF = new Symbol("$", SymbolType.TERMINAL);


    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * @return A map from Symbol to its FIRST set.
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        // Initialize terminals
        for (Symbol t : grammar.getTerminals()) {
            firstSets.put(t, Set.of(t));
        }

        // Ensure epsilon has its set
        if (!firstSets.containsKey(EPSILON)) {
            firstSets.put(EPSILON, Set.of(EPSILON));
        }

        // Initialize non-terminals
        for (Symbol nt : grammar.getNonTerminals()) {
            firstSets.put(nt, new java.util.HashSet<>());
        }
        
        boolean changed;
        do {
            changed = false;
            for (var prod : grammar.getProductions()) {
                Symbol A = prod.getLeft();
                var rhs = prod.getRight();

                boolean allNullable = true;
                for (Symbol Xi : rhs) {
                    for (Symbol sym : firstSets.get(Xi)) {
                        if (!sym.equals(EPSILON)) {
                            changed |= firstSets.get(A).add(sym);
                        }
                    }
                    if (!firstSets.get(Xi).contains(EPSILON)) {
                        allNullable = false;
                        break;
                    }
                }
                if (allNullable) {
                    changed |= firstSets.get(A).add(EPSILON);
                }
            }
        } while (changed);

        return firstSets;
    }

    /**
     * Calculates and returns the FOLLOW sets for non-terminals.
     * @return A map from Symbol to its FOLLOW set.
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        // Ensure that FIRST is calculated
        getFirstSets();

        // Initialize FOLLOW with empty sets
        for (Symbol s : grammar.getNonTerminals()) {
            followSets.put(s, new java.util.HashSet<>());
        }
        // Add EOF to the initial symbol
        followSets.get(grammar.getStartSymbol()).add(EOF);

        boolean changed;
         do {
            changed = false;
            for (var prod : grammar.getProductions()) {
                Symbol A = prod.getLeft();
                var rhs = prod.getRight();

                for (int i = 0; i < rhs.size(); i++) {
                    Symbol Xi = rhs.get(i);
                    if (Xi.type == SymbolType.NON_TERMINAL) {
                        boolean allNullable = true;
                        for (int j = i + 1; j < rhs.size(); j++) {
                            Symbol Xj = rhs.get(j);
                            for (Symbol sym : firstSets.get(Xj)) {
                                if (!sym.equals(EPSILON)) {
                                    changed |= followSets.get(Xi).add(sym);
                                }
                            }
                            if (!firstSets.get(Xj).contains(EPSILON)) {
                                allNullable = false;
                                break;
                            }
                        }
                        if (allNullable) {
                            changed |= followSets.get(Xi).addAll(followSets.get(A));
                        }
                    }
                }
            }
        } while (changed);

        return followSets;
    }
}