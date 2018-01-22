package mcjty.rftools.blocks.shield;

import java.util.HashMap;
import java.util.Map;

public enum DamageTypeMode {
    DAMAGETYPE_GENERIC("Generic"),
    DAMAGETYPE_PLAYER("Player"),
    ;

    private static final Map<String,DamageTypeMode> modeToMode = new HashMap<>();

    private final String description;

    DamageTypeMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static DamageTypeMode getMode(String mode) {
        return modeToMode.get(mode);
    }

    static {
        for (DamageTypeMode mode : values()) {
            modeToMode.put(mode.description, mode);
        }
    }

}
