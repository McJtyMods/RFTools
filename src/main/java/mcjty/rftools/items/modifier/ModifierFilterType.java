package mcjty.rftools.items.modifier;

import java.util.HashMap;
import java.util.Map;

public enum ModifierFilterType {
    FILTER_SLOT("Slot"),
    FILTER_ORE("Ore"),
    FILTER_LIQUID("Liquid"),
    FILTER_TILEENTITY("TE");

    private final String code;

    private static final Map<String, ModifierFilterType> MAP = new HashMap<>();

    static {
        for (ModifierFilterType type : ModifierFilterType.values()) {
            MAP.put(type.getCode(), type);
        }
    }

    ModifierFilterType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static ModifierFilterType getByCode(String code) {
        return MAP.get(code);
    }
}
