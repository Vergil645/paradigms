package expression.calculator;

import expression.TripleExpression;
import expression.exceptions.ConstantFormatException;

public interface Calculator<T> {
    T valueOf(int x);

    T add(T x, T y);

    T subtract(T x, T y);

    T multiply(T x, T y);

    T divide(T x, T y);

    T negate(T x);

    boolean isValidSymbol(char elem);

    TripleExpression<T> parseConst(String str) throws ConstantFormatException;
}
