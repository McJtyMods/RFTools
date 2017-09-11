package mcjty.rftools.blocks.shield;

import net.minecraftforge.common.property.IUnlistedProperty;

public class CamoProperty implements IUnlistedProperty<CamoBlockId> {

    private final String name;

    public CamoProperty(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid(CamoBlockId value) {
        return true;
    }

    @Override
    public Class<CamoBlockId> getType() {
        return CamoBlockId.class;
    }

    @Override
    public String valueToString(CamoBlockId value) {
        return value.toString();
    }
}
