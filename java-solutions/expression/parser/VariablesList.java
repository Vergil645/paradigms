package expression.parser;

import java.util.Set;

public class VariablesList {
    public static final int maxVarLength;

    public static final Set<String> NAMES = Set.of("x", "y", "z");

    static {
        int max = 0;
        for (String str : NAMES) {
            max = Math.max(max, str.length());
        }
        maxVarLength = max;
    }
}
