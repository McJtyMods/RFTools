package mcjty.rftools.shapes;

public enum BeaconType {
    BEACON_PASSIVE(0,1,.3f),
    BEACON_HOSTILE(1,.2f,0),
    BEACON_PLAYER(1,0,1);

    public static final BeaconType[] VALUES;

    private final float r;
    private final float g;
    private final float b;

    static {
        VALUES = new BeaconType[BeaconType.values().length];
        for (int i = 0 ; i < BeaconType.values().length ; i++) {
            VALUES[i] = BeaconType.values()[i];
        }
    }

    BeaconType(float r, float g, float b) {
        this.b = b;
        this.g = g;
        this.r = r;
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
}
