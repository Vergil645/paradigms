package expression.parser;

import expression.*;
import expression.calculator.Calculator;
import expression.exceptions.BracketsBalanceException;
import expression.exceptions.ConstantFormatException;
import expression.exceptions.IllegalSymbolException;
import expression.exceptions.ParseException;

public class ExpressionParser {
    public static <T> CommonExpression<T> parse(String expression, Calculator<T> calc) throws ParseException {
        return new InnerParser<>(expression, calc).parse();
    }

    private static class InnerParser<T> extends BaseParser {
        private final VariablesList<T> variables;
        private final Calculator<T> calc;

        public InnerParser(String data, Calculator<T> calc) {
            super(new StringSource(data));
            this.variables = new VariablesList<>();
            this.calc = calc;
        }

        private CommonExpression<T> parse() throws ParseException {
            CommonExpression<T> res = parseAdditiveGroup();
            if (!eof()) {
                throw new IllegalSymbolException(makeExceptionMessage("binary operation"));
            }
            return res;
        }

        private CommonExpression<T> parseAdditiveGroup() throws ParseException {
            CommonExpression<T> exp = parseMultiplicativeGroup();
            while (true) {
                if (test('+')) {
                    exp = new Add<>(exp, parseMultiplicativeGroup());
                } else if (test('-')) {
                    exp = new Subtract<>(exp, parseMultiplicativeGroup());
                } else {
                    return exp;
                }
            }
        }

        private CommonExpression<T> parseMultiplicativeGroup() throws ParseException {
            CommonExpression<T> exp = parseElement();
            while (true) {
                if (test('*')) {
                    exp = new Multiply<>(exp, parseElement());
                } else if (test('/')) {
                    exp = new Divide<>(exp, parseElement());
                } else {
                    return exp;
                }
            }
        }

        private CommonExpression<T> parseElement() throws ParseException {
            skipWhitespace();
            CommonExpression<T> res;
            if (test('-')) {
                res = calc.isValidSymbol(getChar()) ? parseConst( "-") : new Negate<>(parseElement());
            } else if (test('(')) {
                res = parseAdditiveGroup();
                if (!test(')')) {
                    throw new BracketsBalanceException(makeExceptionMessage(")"));
                }
            } else if (calc.isValidSymbol(getChar())) {
                res = parseConst("");
            } else {
                res = parseVariable();
            }
            if (res == null) {
                throw new IllegalSymbolException(
                        makeExceptionMessage("constant, variable, unary operation or bracket expression")
                );
            }
            skipWhitespace();
            return res;
        }

        private CommonExpression<T> parseConst(String sign) throws ConstantFormatException {
            StringBuilder sb = new StringBuilder(sign);
            while (calc.isValidSymbol(getChar())) {
                sb.append(nextChar());
            }
            try {
                return calc.parseConst(sb.toString());
            } catch (ConstantFormatException e) {
                throw new ConstantFormatException(String.format(
                        "Invalid constant on positions %d - %d: %s", getPos() - sb.length() + 1, getPos(), sb
                ));
            }
        }

        private CommonExpression<T> parseVariable() {
            for (int len = variables.maxVarLength; len > 0; len--) {
                String sub = substring(len);
                if (variables.NAMES.containsKey(sub) && testId(sub)) {
                    return variables.NAMES.get(sub);
                }
            }
            return null;
        }

        private boolean testId(final String expected) {
            return test(expected, !Character.isLetterOrDigit(charAt(expected.length())));
        }

        private String makeExceptionMessage(String expected) {
            return String.format("Expected: %s on position %d\nFound: %c", expected, getPos() + 1, getChar());
        }

        private void skipWhitespace() {
            while (test(' ') || test('\r') || test('\n') || test('\t'));
        }
    }
}
