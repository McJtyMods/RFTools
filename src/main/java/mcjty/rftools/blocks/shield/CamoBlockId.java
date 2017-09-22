package mcjty.rftools.blocks.shield;

public class CamoBlockId {
    private final String registryName;
    private final int meta;

    public CamoBlockId(String registryName, int meta) {
        this.registryName = registryName;
        this.meta = meta;
    }

    public String getRegistryName() {
        return registryName;
    }

    public int getMeta() {
        return meta;
    }

    @Override
    public String toString() {
        return registryName + '@' + meta;
    }
}
