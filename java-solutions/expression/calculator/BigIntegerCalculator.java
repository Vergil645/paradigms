package expression.calculator;

import expression.Const;
import expression.CommonExpression;
import expression.exceptions.ConstantFormatException;
import expression.exceptions.DivisionByZeroException;

import java.math.BigInteger;

public class BigIntegerCalculator implements Calculator<BigInteger> {
    @Override
    public BigInteger valueOf(int x) {
        return BigInteger.valueOf(x);
    }

    @Override
    public BigInteger add(BigInteger x, BigInteger y) {
        return x.add(y);
    }

    @Override
    public BigInteger subtract(BigInteger x, BigInteger y) {
        return x.subtract(y);
    }

    @Override
    public BigInteger multiply(BigInteger x, BigInteger y) {
        return x.multiply(y);
    }

    @Override
    public BigInteger divide(BigInteger x, BigInteger y) {
        if (y.equals(BigInteger.ZERO)) {
            throw new DivisionByZeroException(String.format("Division by zero: %d / %d", x, y));
        }
        return x.divide(y);
    }

    @Override
    public BigInteger negate(BigInteger x) {
        return x.negate();
    }

    @Override
    public boolean isValidSymbol(char elem) {
        return '0' <= elem && elem <= '9';
    }

    @Override
    public CommonExpression<BigInteger> parseConst(String str) throws ConstantFormatException {
        try {
            return new Const<>(new BigInteger(str));
        } catch (NumberFormatException e) {
            throw new ConstantFormatException("Invalid constant: " + str);
        }
    }
}
