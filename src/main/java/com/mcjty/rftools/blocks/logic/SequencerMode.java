package com.mcjty.rftools.blocks.logic;

import java.util.HashMap;
import java.util.Map;

public enum SequencerMode {
    MODE_ONCE1(0, "Once1"),             // Cycle once as soon as a redstone signal is received. Ignores new signals until cycleBits is done
    MODE_ONCE2(1, "Once2"),             // Cycle once as soon as a redstone signal is received. Restarts cycleBits if a new redstone signal arrives
    MODE_LOOP1(2, "Loop1"),             // Cycle all the time. Ignore redstone signals
    MODE_LOOP2(3, "Loop2"),             // Cycle all the time. Restone signal sets cycle to the beginning
    MODE_LOOP3(4, "Loop3"),             // Cycle for as long as a redstone signal is given. Stop as soon as the signal ends
    MODE_STEP(5, "Step"),               // Proceed one step in the cycleBits every time a redstone signal comes in
    ;

    public static final Map<String,Integer> modeToMode = new HashMap<String, Integer>();

    private final int index;
    private final String description;

    SequencerMode(int index, String description) {
        this.index = index;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    static {
        for (SequencerMode mode : values()) {
            modeToMode.put(mode.description, mode.index);
        }
    }
}
