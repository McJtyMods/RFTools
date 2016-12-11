package mcjty.rftools.blocks.logic.sensor;

import mcjty.rftools.varia.NamedEnum;

public enum SensorType implements NamedEnum {
    SENSOR_BLOCK("Block", false, true, "Detect if a certain type", "of block is present"),
    SENSOR_FLUID("Fluid", false, true, "Detect if a certain type", "of fluid is present"),
    SENSOR_GROWTHLEVEL("Growth", true, true, "Detect the growth percentage", "of a crop"),
    SENSOR_ENTITIES("Entities", true, false, "Count the amount of entities"),
    SENSOR_ITEMS("Items", true, false, "Count the amount of items"),
    SENSOR_PLAYERS("Players", true, false, "Count the amount of players"),
    SENSOR_HOSTILE("Hostile", true, false, "Count the amount of hostile mobs"),
    SENSOR_PASSIVE("Passive", true, false, "Count the amount of passive mobs");

    private final String name;
    private final String[] description;
    private final boolean supportsNumber;
    private final boolean supportsGroup;

    SensorType(String name, boolean supportsNumber, boolean supportsGroup, String... description) {
        this.name = name;
        this.supportsNumber = supportsNumber;
        this.supportsGroup = supportsGroup;
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

    public boolean isSupportsNumber() {
        return supportsNumber;
    }

    public boolean isSupportsGroup() {
        return supportsGroup;
    }
}
