package mcjty.rftools.blocks.logic.sequencer;

import java.util.HashMap;
import java.util.Map;

public enum SequencerMode {
    MODE_ONCE1("Once1"),             // Cycle once as soon as a redstone signal is received. Ignores new signals until cycleBits is done
    MODE_ONCE2("Once2"),             // Cycle once as soon as a redstone signal is received. Restarts cycleBits if a new redstone signal arrives
    MODE_LOOP1("Loop1"),             // Cycle all the time. Ignore redstone signals
    MODE_LOOP2("Loop2"),             // Cycle all the time. Restone signal sets cycle to the beginning
    MODE_LOOP3("Loop3"),             // Cycle for as long as a redstone signal is given. Stop as soon as the signal ends and stay at the cycle step.
    MODE_STEP("Step"),               // Proceed one step in the cycleBits every time a redstone signal comes in
    MODE_LOOP4("Loop4"),             // Cycle for as long as a redstone signal is given. Stop as soon as the signal ends and resets to the beginning.
    ;

    private static final Map<String,SequencerMode> modeToMode = new HashMap<>();

    private final String description;

    SequencerMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static SequencerMode getMode(String mode) {
        return modeToMode.get(mode);
    }

    static {
        for (SequencerMode mode : values()) {
            modeToMode.put(mode.description, mode);
        }
    }
}
