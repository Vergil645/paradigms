package expression.parser;

import expression.*;
import expression.exceptions.*;
import expression.calculator.Calculator;

public class ExpressionParser {
    public static <T> CommonExpression<T> parse(String expression, Calculator<T> calc) throws ParseException {
        return new InnerParser<>(expression, calc).parse();
    }

    private static class InnerParser<T> extends BaseParser {
        private final Calculator<T> calc;

        public InnerParser(String data, Calculator<T> calc) {
            super(new StringSource(data));
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
                    exp = new Add<>(calc, exp, parseMultiplicativeGroup());
                } else if (test('-')) {
                    exp = new Subtract<>(calc, exp, parseMultiplicativeGroup());
                } else {
                    return exp;
                }
            }
        }

        private CommonExpression<T> parseMultiplicativeGroup() throws ParseException {
            CommonExpression<T> exp = parseElement();
            while (true) {
                if (test('*')) {
                    exp = new Multiply<>(calc, exp, parseElement());
                } else if (test('/')) {
                    exp = new Divide<>(calc, exp, parseElement());
                } else if (testId("mod")) {
                    exp = new Mod<>(calc, exp, parseElement());
                } else {
                    return exp;
                }
            }
        }

        private CommonExpression<T> parseElement() throws ParseException {
            skipWhitespace();
            CommonExpression<T> res;
            if (test('-')) {
                res = calc.isValidSymbol(getChar()) ? parseConst( "-") : new Negate<>(calc, parseElement());
            } else if (testId("abs")) {
                res = new Abs<>(calc, parseElement());
            } else if (testId("square")) {
                res = new Square<>(calc, parseElement());
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
                return new Const<>(calc.valueOf(sb.toString()));
            } catch (IllegalArgumentException e) {
                throw new ConstantFormatException(String.format(
                        "Invalid constant on positions %d - %d: %s", getPos() - sb.length() + 1, getPos(), sb
                ));
            }
        }

        private CommonExpression<T> parseVariable() {
            for (int len = VariablesList.maxVarLength; len > 0; len--) {
                String sub = substring(len);
                if (VariablesList.NAMES.contains(sub) && testId(sub)) {
                    return new Variable<>(calc, sub);
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
