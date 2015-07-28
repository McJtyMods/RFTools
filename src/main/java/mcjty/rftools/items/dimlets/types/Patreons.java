package mcjty.rftools.items.dimlets.types;

public enum Patreons {
    PATREON_FIREWORKS(0),
    PATREON_SICKMOON(1),
    PATREON_SICKSUN(2),
    PATREON_PINKPILLARS(3),
    PATREON_RABBITMOON(4),
    PATREON_RABBITSUN(5),
    PATREON_PUPPETEER(6),
    PATREON_LAYEREDMETA(7),
    PATREON_COLOREDPRISMS(8);

    private final int bit;

    Patreons(int bit) {
        this.bit = bit;
    }

    public int getBit() {
        return bit;
    }
}
