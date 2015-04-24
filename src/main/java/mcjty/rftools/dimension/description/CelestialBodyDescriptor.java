package mcjty.rftools.dimension.description;

import mcjty.rftools.dimension.world.types.CelestialBodyType;

public class CelestialBodyDescriptor {
    private final CelestialBodyType type;
    private final boolean main;         // True if this is a main body (controlled by base time of dimension).

    public CelestialBodyDescriptor(CelestialBodyType type, boolean main) {
        this.type = type;
        this.main = main;
    }

    public CelestialBodyType getType() {
        return type;
    }

    public boolean isMain() {
        return main;
    }
}
