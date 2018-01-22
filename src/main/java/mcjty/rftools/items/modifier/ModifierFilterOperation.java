package mcjty.rftools.items.modifier;

import java.util.HashMap;
import java.util.Map;

public enum ModifierFilterOperation {
    OPERATION_SLOT("Slot"),
    OPERATION_VOID("Void");

    private final String code;

    private static final Map<String, ModifierFilterOperation> MAP = new HashMap<>();

    static {
        for (ModifierFilterOperation type : ModifierFilterOperation.values()) {
            MAP.put(type.getCode(), type);
        }
    }

    ModifierFilterOperation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ModifierFilterOperation getByCode(String code) {
        return MAP.get(code);
    }
}
