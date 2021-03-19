package expression.calculator;

import expression.CommonExpression;
import expression.exceptions.ConstantFormatException;

public interface Calculator<T> {
    T valueOf(int x);

    T add(T x, T y);

    T subtract(T x, T y);

    T multiply(T x, T y);

    T divide(T x, T y);

    T negate(T x);

    T abs(T x);

    T square(T x);

    T mod (T x, T y);

    boolean isValidSymbol(char elem);

    CommonExpression<T> parseConst(String str) throws ConstantFormatException;
}
