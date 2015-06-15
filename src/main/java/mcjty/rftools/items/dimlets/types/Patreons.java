package mcjty.rftools.items.dimlets.types;

public enum Patreons {
    PATREON_FIREWORKS(0),
    PATREON_SICKMOON(1),
    PATREON_SICKSUN(2),
    PATREON_PINKGRASS(3);

    private final int bit;

    Patreons(int bit) {
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }
}
