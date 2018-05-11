package mcjty.rftools.blocks.logic.sensor;

import mcjty.rftools.varia.NamedEnum;

public enum AreaType implements NamedEnum {
    AREA_1("Area 1", 1, "1 block in front of sensor"),
    AREA_3("Area 3", 3, "3 blocks in front of sensor"),
    AREA_5("Area 5", 5, "5 blocks in front of sensor"),
    AREA_3X3("Area 3x3", -3, "3x3 blocks in front of sensor"),
    AREA_5X5("Area 5x5", -5, "5x5 blocks in front of sensor"),
    AREA_7X7("Area 7x7", -7, "7x7 blocks in front of sensor");

    private final String name;
    private final String[] description;
    private final int blockCount;

    AreaType(String name, int blockCount, String... description) {
        this.name = name;
        this.blockCount = blockCount;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String[] getDescription() {
        return description;
    }

    public int getBlockCount() {
        return blockCount;
    }
}
