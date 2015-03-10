package com.mcjty.rftools.blocks;

import java.util.HashMap;
import java.util.Map;

public enum RedstoneMode {
    REDSTONE_IGNORED("Ignored"),
    REDSTONE_OFFREQUIRED("Off"),
    REDSTONE_ONREQUIRED("On");

    private static final Map<String,RedstoneMode> modeToMode = new HashMap<String, RedstoneMode>();

    private final String description;

    RedstoneMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static RedstoneMode getMode(String mode) {
        return modeToMode.get(mode);
    }

    static {
        for (RedstoneMode mode : values()) {
            modeToMode.put(mode.description, mode);
        }
    }

}
