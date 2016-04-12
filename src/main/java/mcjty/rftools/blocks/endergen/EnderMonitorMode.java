package mcjty.rftools.blocks.endergen;

import java.util.HashMap;
import java.util.Map;

public enum EnderMonitorMode {
    MODE_LOSTPEARL("Lost Pearl"),
    MODE_PEARLFIRED("Pearl Fired"),
    MODE_PEARLARRIVED("Pearl Arrived"),
    ;

    private static final Map<String,EnderMonitorMode> modeToMode = new HashMap<String, EnderMonitorMode>();

    private final String description;

    EnderMonitorMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static EnderMonitorMode getMode(String mode) {
        return modeToMode.get(mode);
    }


    static {
        for (EnderMonitorMode mode : values()) {
            modeToMode.put(mode.description, mode);
        }
    }

}
