package expression.generic;

import expression.calculator.*;
import expression.parser.*;
import expression.exceptions.*;
import expression.*;

public class GenericTabulator implements Tabulator {
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                throw new IllegalArgumentException("Invalid arguments: expected 2 arguments\nFound: " + args.length);
            }
            System.out.format("Mode: %s\nExpression: %s\n", args[0], args[1]);

            Object[][][] res = new GenericTabulator().tabulate(
                    args[0].substring(1), args[1], -2, 2, -2, 2, -2, 2
            );

            System.out.println("Results:");
            for (int i = 0; i <= 4; i++) {
                for (int j = 0; j <= 4; j++) {
                    for (int k = 0; k <= 4; k++) {
                        System.out.format("\tf(x = %d, y = %d, z = %d) = %s\n", -2 + i, -2 + j, -2 + k, res[i][j][k]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object[][][] tabulate(
            String mode, String expression,
            int x1, int x2, int y1, int y2, int z1, int z2
    ) throws Exception {
        if (!ModeList.CALCULATORS.containsKey(mode)) {
            throw new IllegalArgumentException(String.format(
                    "Illegal mode: %s\nValid modes:\n%s", mode, ModeList.VALID_MODES
            ));
        }
        return tabulate(ModeList.CALCULATORS.get(mode), expression, x1, x2, y1, y2, z1, z2);
    }

    private <T> Object[][][] tabulate(
            Calculator<T> calc, String expression,
            int x1, int x2, int y1, int y2, int z1, int z2
    ) throws ParseException {
        Object[][][] res = new Object[x2 - x1 + 1][y2 - y1 + 1][z2 - z1 + 1];
        CommonExpression<T> expr = ExpressionParser.parse(expression, calc);
        for (int i = 0; i <= x2 - x1; i++) {
            for (int j = 0; j <= y2 - y1; j++) {
                for (int k = 0; k <= z2 - z1; k++) {
                    try {
                        res[i][j][k] = expr.evaluate(calc, x1 + i, y1 + j, z1 + k);
                    } catch (Exception e) {
                        res[i][j][k] = null;
                    }
                }
            }
        }
        return res;
    }
}
