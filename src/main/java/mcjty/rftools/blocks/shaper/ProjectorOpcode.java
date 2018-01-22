package mcjty.rftools.blocks.shaper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ProjectorOpcode {
    NONE("-", "Do nothing", false),
    ON("On", "Turn projection on", false),
    OFF("Off", "Turn projection off", false),
    SCAN("Scn", "Perform a refresh of the scan (pulse only)", false),
    OFFSET("Ofs", "Gradually move the offset of the scan to the destination value", true),
    ROT("Rot", "Gradually rotate the angle of the scan to the destination value", true),
    SCALE("Scl", "Gradually scale the scan to the destination value", true),
    GRAYON("Gr+", "Turn on grayscale mode", false),
    GRAYOFF("Gr-", "Turn off grayscale mode", false);

    private final String code;
    private final String description;
    private final boolean needsValue;

    private static final Map<String, ProjectorOpcode> MAP = new HashMap<>();
    private static final String[] choices;

    static {
        List<String> choiceList = new ArrayList<>();
        for (ProjectorOpcode operation : ProjectorOpcode.values()) {
            MAP.put(operation.getCode(), operation);
            choiceList.add(operation.getCode());
        }
        choices = choiceList.toArray(new String[choiceList.size()]);
    }

    ProjectorOpcode(String code, String description, boolean needsValue) {
        this.code = code;
        this.description = description;
        this.needsValue = needsValue;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isNeedsValue() {
        return needsValue;
    }

    public static ProjectorOpcode getByCode(String code) {
        return MAP.get(code);
    }

    public static String[] getChoices() {
        return choices;
    }
}
