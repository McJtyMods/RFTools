package com.mcjty.rftools.items.dimlets;

import java.util.*;

public enum DimletType {
    DIMLET_BIOME("biomeDimlet", "Biome", "B", false, null, "This dimlet controls the biomes that can generate in a dimension", "The controller specifies how they can be used."),
    DIMLET_FOLIAGE("foliageDimlet", "Foliage", "F", false, null, "WIP"),
    DIMLET_LIQUID("liquidDimlet", "Liquid", "L", true, null, "This is a modifier for terrain, lake, or liquid orbs.", "Put these dimlets BEFORE the thing you want", "to change."),
    DIMLET_MATERIAL("materialDimlet", "Material", "m", true, null, "This is a modifier for terrain, tendrils, canyons, orbs,", "liquid orbs, or oregen.", "Put these dimlets BEFORE the thing you want", "to change."),
    DIMLET_MOBS("mobsDimlet", "Mob", "M", false, null, "Control what type of mobs can spawn", "in addition to normal mob spawning."),
    DIMLET_SKY("skyDimlet", "Sky", "s", false, null, "Control various features of the sky", "like sky color, fog color, celestial bodies, ..."),
    DIMLET_STRUCTURE("structuresDimlet", "Structure", "S", false, null, "Control generation of various structures", "in the world."),
    DIMLET_TERRAIN("terrainDimlet", "Terrain", "T", false, new DimletType[] { DIMLET_MATERIAL, DIMLET_LIQUID }, "This affects the type of terrain", "that you will get in a dimension", "This dimlet can receive liquid and material", "modifiers which have to come in front of the terrain."),
    DIMLET_FEATURE("featureDimlet", "Feature", "f", false, new DimletType[] { DIMLET_MATERIAL, DIMLET_LIQUID }, "This affects various features of the dimension.", "Some of these features need material or liquid modifiers", "which you have to put in front of this feature."),
    DIMLET_TIME("timeDimlet", "Time", "t", false, null, "Control the flow of time."),
    DIMLET_DIGIT("digitDimlet", "Digit", "d", false, null, "This dimlet has no effect on the dimension", "but can be used to get new unique dimensions", "with exactly the same dimlets."),
    DIMLET_EFFECT("effectDimlet", "Effect", "e", false, null, "Control various environmental effects", "in the dimension."),
    DIMLET_SPECIAL("specialDimlet", "Special", "X", false, null, "Special dimlets with various features."),
    DIMLET_CONTROLLER("controllerDimlet", "Controller", "C", false, null, "A biome controller will affect how biomes", "are used in this dimension."),
    DIMLET_WEATHER("weatherDimlet", "Weather", "W", false, null, "A weather dimlet affects the weather", "in a dimension.");

    private final String textureName;
    private final String name;
    private final String opcode;
    private final boolean isModifier;
    private final Set<DimletType> modifierTypes;
    private final List<String> information;

    private static final Map<String,DimletType> typeByName = new HashMap<String, DimletType>();
    private static final Map<String,DimletType> typeByOpcode = new HashMap<String, DimletType>();

    static {
        for (DimletType type : values()) {
            typeByName.put(type.getName(), type);
            typeByOpcode.put(type.getOpcode(), type);
        }
    }

    DimletType(String textureName, String name, String opcode, boolean isModifier, DimletType[] modifierTypes, String... info) {
        this.textureName = textureName;
        this.name = name;
        this.opcode = opcode;
        this.isModifier = isModifier;
        this.modifierTypes = new HashSet<DimletType>();
        if (modifierTypes != null) {
            Collections.addAll(this.modifierTypes, modifierTypes);
        }
        this.information = new ArrayList<String>();
        Collections.addAll(information, info);

    }

    public String getName() {
        return name;
    }

    public String getTextureName() {
        return textureName;
    }

    public String getOpcode() {
        return opcode;
    }

    public static DimletType getTypeByName(String name) {
        return typeByName.get(name);
    }

    public static DimletType getTypeByOpcode(String opcode) {
        return typeByOpcode.get(opcode);
    }

    public boolean isModifier() {
        return isModifier;
    }

    /**
     * Return true if this type can be modified by the given type.
     */
    public boolean isModifiedBy(DimletType type) {
        return modifierTypes.contains(type);
    }

    public List<String> getInformation() {
        return information;
    }
}
