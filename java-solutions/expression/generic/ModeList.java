package expression.generic;

import expression.calculator.*;
import java.util.Map;

public class ModeList {
    public static final Map<String, Calculator<?>> CALCULATORS = Map.ofEntries(
            Map.entry("i", new IntegerCalculator()),
            Map.entry("d", new DoubleCalculator()),
            Map.entry("bi", new BigIntegerCalculator()),
            Map.entry("u", new UncheckedIntegerCalculator()),
            Map.entry("p", new ModIntegerCalculator()),
            Map.entry("b", new ByteCalculator())
    );

    public static final String VALID_MODES;

    static {
        StringBuilder tmp = new StringBuilder();
        for (String mode : CALCULATORS.keySet()) {
            tmp.append("\t").append(mode).append("\n");
        }
        if (!tmp.isEmpty()) {
            tmp.deleteCharAt(tmp.length() - 1);
        }
        VALID_MODES = tmp.toString();
    }
}
