package mcjty.rftools.dimension.world.types;

public enum StructureType {
    STRUCTURE_NONE("None"),
    STRUCTURE_VILLAGE("Village"),
    STRUCTURE_STRONGHOLD("Stronghold"),
    STRUCTURE_DUNGEON("Dungeon"),
    STRUCTURE_FORTRESS("Fortress"),
    STRUCTURE_MINESHAFT("Mineshaft"),
    STRUCTURE_SCATTERED("Scattered"),
    STRUCTURE_RECURRENTCOMPLEX("RecurrentComplex");

    private final String name;

    StructureType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
