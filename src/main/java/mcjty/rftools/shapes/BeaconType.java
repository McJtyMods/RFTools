package mcjty.rftools.shapes;

import java.util.HashMap;
import java.util.Map;

public enum BeaconType {
    BEACON_OFF(0.1f,0.1f,0.1f, "off"),
    BEACON_GREEN(0,1,0, "green"),
    BEACON_RED(1,0,0, "red"),
    BEACON_CYAN(0,1,1, "cyan"),
    BEACON_BLUE(0,0,1, "blue"),
    BEACON_YELLOW(1,1,0, "yellow"),
    BEACON_PURPLE(1,0,1, "purple"),
    BEACON_WHITE(1,1,1, "white");

    public static final BeaconType[] VALUES;
    private static final Map<String, BeaconType> TYPE_BY_CODE = new HashMap<>();

    private final String code;
    private final float r;
    private final float g;
    private final float b;
    private final int color;

    static {
        VALUES = new BeaconType[BeaconType.values().length];
        for (int i = 0 ; i < BeaconType.values().length ; i++) {
            VALUES[i] = BeaconType.values()[i];
            TYPE_BY_CODE.put(VALUES[i].getCode(), VALUES[i]);
        }
    }

    BeaconType(float r, float g, float b, String code) {
        this.code = code;
        this.b = b;
        this.g = g;
        this.r = r;
        color = (((int) (r * 255.0)) << 16) + (((int) (g * 255.0)) << 8) + (int) (b * 255.0);
    }

    public String getCode() {
        return code;
    }

    public int getColor() {
        return color;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public static BeaconType getTypeByCode(String code) {
        if (code == null || code.isEmpty()) {
            return BEACON_OFF;
        }
        BeaconType type = TYPE_BY_CODE.get(code);
        if (type == null) {
            return BEACON_OFF;
        }
        return type;
    }
}
