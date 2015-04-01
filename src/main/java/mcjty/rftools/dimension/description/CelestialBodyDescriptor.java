package mcjty.rftools.dimension.description;

import mcjty.rftools.dimension.world.types.CelestialBodyType;

public class CelestialBodyDescriptor {
    private final CelestialBodyType type;
    private final float timeOffset;
    private final float timeFactor;
    private final float yAngle;

    public CelestialBodyDescriptor(CelestialBodyType type, float timeOffset, float timeFactor, float yAngle) {
        this.type = type;
        this.timeOffset = timeOffset;
        this.timeFactor = timeFactor;
        this.yAngle = yAngle;
    }

    public CelestialBodyType getType() {
        return type;
    }

    public float getTimeOffset() {
        return timeOffset;
    }

    public float getTimeFactor() {
        return timeFactor;
    }

    public float getyAngle() {
        return yAngle;
    }
}
