package mcjty.rftools.blocks.shield;

import java.util.HashMap;
import java.util.Map;

public enum ShieldRenderingMode {
    MODE_INVISIBLE("Invisible"),
    MODE_SHIELD("Shield"),
    MODE_SOLID("Solid"),
    ;

    private static final Map<String,ShieldRenderingMode> modeToMode = new HashMap<String, ShieldRenderingMode>();

    private final String description;

    ShieldRenderingMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ShieldRenderingMode getMode(String mode) {
        return modeToMode.get(mode);
    }

    static {
        for (ShieldRenderingMode mode : values()) {
            modeToMode.put(mode.description, mode);
        }
    }

}
