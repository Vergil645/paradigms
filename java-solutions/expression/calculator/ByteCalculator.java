package expression.calculator;

import expression.Const;
import expression.CommonExpression;
import expression.exceptions.ConstantFormatException;

public class ByteCalculator implements Calculator<Byte> {
    @Override
    public Byte valueOf(int x) {
        return (byte) x;
    }

    @Override
    public Byte add(Byte x, Byte y) {
        return (byte) (x + y);
    }

    @Override
    public Byte subtract(Byte x, Byte y) {
        return (byte) (x - y);
    }

    @Override
    public Byte multiply(Byte x, Byte y) {
        return (byte) (x * y);
    }

    @Override
    public Byte divide(Byte x, Byte y) {
        return (byte) (x / y);
    }

    @Override
    public Byte negate(Byte x) {
        return (byte) (-x);
    }

    @Override
    public Byte abs(Byte x) {
        return x >= 0 ? x : negate(x);
    }

    @Override
    public Byte square(Byte x) {
        return (byte) (x * x);
    }

    @Override
    public Byte mod(Byte x, Byte y) {
        return (byte) (x % y);
    }

    @Override
    public boolean isValidSymbol(char elem) {
        return '0' <= elem && elem <= '9';
    }

    @Override
    public CommonExpression<Byte> parseConst(String str) throws ConstantFormatException {
        try {
            return new Const<>(Byte.parseByte(str));
        } catch (NumberFormatException e) {
            throw new ConstantFormatException("Invalid constant: " + str);
        }
    }
}
