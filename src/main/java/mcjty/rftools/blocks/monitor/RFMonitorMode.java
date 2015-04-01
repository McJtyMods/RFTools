package mcjty.rftools.blocks.monitor;

public enum RFMonitorMode {
    MODE_OFF (0, "Off"),
    MODE_LESS (1, "Less"),
    MODE_MORE (2, "More");

    private final int index;
    private final String description;

    RFMonitorMode(int index, String description) {
        this.index = index;
        this.description = description;
    }

    public int getIndex() {
        return index;
    }

    public String getDescription() {
        return description;
    }

    public static RFMonitorMode getModeFromIndex(int index) {
        for (RFMonitorMode mode : values()) {
            if (mode.getIndex() == index) {
                return mode;
            }
        }
        return MODE_OFF;
    }

    public static RFMonitorMode getModeFromDescription(String description) {
        for (RFMonitorMode mode : values()) {
            if (description.equals(mode.getDescription())) {
                return mode;
            }
        }
        return MODE_OFF;
    }
}
