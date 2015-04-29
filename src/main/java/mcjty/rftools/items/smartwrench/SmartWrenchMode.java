package mcjty.rftools.items.smartwrench;

import java.util.HashMap;
import java.util.Map;

public enum SmartWrenchMode {
    MODE_WRENCH("w", "wrench"),
    MODE_SELECT("s", "select");

    private static Map<String,SmartWrenchMode> codeToMode = new HashMap<String, SmartWrenchMode>();

    private final String code;
    private final String name;

    static {
        for (SmartWrenchMode mode : values()) {
            codeToMode.put(mode.getCode(), mode);
        }
    }

    SmartWrenchMode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static SmartWrenchMode getMode(String code) {
        return codeToMode.get(code);
    }
}
