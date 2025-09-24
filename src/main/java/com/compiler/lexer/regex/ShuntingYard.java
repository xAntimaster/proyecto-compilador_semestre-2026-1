package com.compiler.lexer.regex;

/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 * <p>
 * Provides methods to preprocess regular expressions by inserting explicit
 * concatenation operators, and to convert infix regular expressions to postfix
 * notation for easier parsing and NFA construction.
 */
public class ShuntingYard {

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // No se requiere implementación especial
    }

    /**
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < regex.length(); i++) {
            char c1 = regex.charAt(i);
            output.append(c1);

            if (i + 1 < regex.length()) {
                char c2 = regex.charAt(i + 1);

                // Concatenación implícita:
                if ((isOperand(c1) || c1 == ')' || c1 == '*' || c1 == '+' || c1 == '?')
                        && (isOperand(c2) || c2 == '(')) {
                    output.append('·');
                }
            }
        }
        return output.toString();
    }

    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    private static boolean isOperand(char c) {
        return !(c == '|' || c == '*' || c == '?' || c == '+' || c == '(' || c == ')' || c == '·');
    }

    /**
     * Converts an infix regular expression to postfix notation using the
     * Shunting Yard algorithm. This is useful for constructing NFAs from
     * regular expressions.
     *
     * @param infixRegex Regular expression in infix notation.
     * @return Regular expression in postfix notation.
     */
    public static String toPostfix(String infixRegex) {
        String regex = insertConcatenationOperator(infixRegex);
        StringBuilder output = new StringBuilder();

        // Implementamos un stack con arreglo de chars
        char[] stack = new char[regex.length()];
        int top = -1;

        for (int i = 0; i < regex.length(); i++) {
            char token = regex.charAt(i);

            if (isOperand(token)) {
                output.append(token);
            } else if (token == '(') {
                stack[++top] = token;
            } else if (token == ')') {
                while (top >= 0 && stack[top] != '(') {
                    output.append(stack[top--]);
                }
                if (top >= 0 && stack[top] == '(') {
                    top--; // pop '('
                }
            } else { // operador
                while (top >= 0 && stack[top] != '(') {
                    char op = stack[top];

                    int precOp = (op == '*' || op == '+' || op == '?') ? 3
                                : (op == '·') ? 2
                                : (op == '|') ? 1
                                : 0;
                    int precTok = (token == '*' || token == '+' || token == '?') ? 3
                                : (token == '·') ? 2
                                : (token == '|') ? 1
                                : 0;

                    if (precOp >= precTok) {
                        output.append(stack[top--]);
                    } else {
                        break;
                    }
                }
                stack[++top] = token;
            }
        }

        while (top >= 0) {
            output.append(stack[top--]);
        }

        return output.toString();
    }
}