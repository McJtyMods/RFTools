package com.mcjty.rftools.items.dimlets;

import java.util.*;

public enum DimletType {
    DIMLET_BIOME("biomeDimlet", "Biome", "B", false, null),
    DIMLET_FOLIAGE("foliageDimlet", "Foliage", "F", false, null),
    DIMLET_LIQUID("liquidDimlet", "Liquid", "L", false, null),
    DIMLET_MATERIAL("materialDimlet", "Material", "m", true, null),
    DIMLET_MOBS("mobsDimlet", "Mob", "M", false, null),
    DIMLET_SKY("skyDimlet", "Sky", "s", false, null),
    DIMLET_STRUCTURE("structuresDimlet", "Structure", "S", false, null),
    DIMLET_TERRAIN("terrainDimlet", "Terrain", "T", false, new DimletType[] { DIMLET_MATERIAL }),
    DIMLET_FEATURE("featureDimlet", "Feature", "f", false, null),
    DIMLET_TIME("timeDimlet", "Time", "t", false, null),
    DIMLET_DIGIT("digitDimlet", "Digit", "d", false, null);

    private final String textureName;
    private final String name;
    private final String opcode;
    private final boolean isModifier;
    private final Set<DimletType> modifierTypes;

    private static final Map<String,DimletType> typeByName = new HashMap<String, DimletType>();
    private static final Map<String,DimletType> typeByOpcode = new HashMap<String, DimletType>();

    static {
        for (DimletType type : values()) {
            typeByName.put(type.getName(), type);
            typeByOpcode.put(type.getOpcode(), type);
        }
    }

    DimletType(String textureName, String name, String opcode, boolean isModifier, DimletType[] modifierTypes) {
        this.textureName = textureName;
        this.name = name;
        this.opcode = opcode;
        this.isModifier = isModifier;
        this.modifierTypes = new HashSet<DimletType>();
        if (modifierTypes != null) {
            for (DimletType type : modifierTypes) {
                this.modifierTypes.add(type);
            }
        }
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

    public Set<DimletType> getModifierTypes() {
        return modifierTypes;
    }

    /**
     * Return true if this type can be modified by the given type.
     */
    public boolean isModifiedBy(DimletType type) {
        return modifierTypes.contains(type);
    }
}
