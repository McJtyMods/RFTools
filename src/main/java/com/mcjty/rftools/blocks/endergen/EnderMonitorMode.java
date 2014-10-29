package com.mcjty.rftools.blocks.endergen;

import java.util.HashMap;
import java.util.Map;

public enum EnderMonitorMode {
    MODE_LOSTPEARL(0, "Lost Pearl"),
    MODE_PEARLFIRED(1, "Pearl Fired"),
    MODE_PEARLARRIVED(2, "Pearl Arrived"),
    ;

    public static final Map<String,Integer> modeToMode = new HashMap<String, Integer>();

    private final int index;
    private final String description;

    EnderMonitorMode(int index, String description) {
        this.index = index;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    static {
        for (EnderMonitorMode mode : values()) {
            modeToMode.put(mode.description, mode.index);
        }
    }


}
