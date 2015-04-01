package mcjty.rftools.dimension.world.types;

public enum CelestialBodyType {
    BODY_NONE(0, 0),
    BODY_SUN(3, 0),
    BODY_LARGESUN(4,0),
    BODY_SMALLSUN(2,0),
    BODY_REDSUN(1,0),
    BODY_MOON(0,3),
    BODY_LARGEMOON(0,4),
    BODY_SMALLMOON(0,2),
    BODY_REDMOON(0,1),
    BODY_PLANET(0,0),
    BODY_LARGEPLANET(0,0);

    private final int goodSunFactor;
    private final int goodMoonFactor;

    CelestialBodyType(int goodSunFactor, int goodMoonFactor) {
        this.goodSunFactor = goodSunFactor;
        this.goodMoonFactor = goodMoonFactor;
    }

    public int getGoodSunFactor() {
        return goodSunFactor;
    }

    public int getGoodMoonFactor() {
        return goodMoonFactor;
    }
}
