package mcjty.rftools.blocks.shield;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class NoTickCamoShieldBlock extends CamoShieldBlock {

    @Override
    protected void init() {
        setRegistryName("notick_camo_shield_block");
        setUnlocalizedName("rftools.notick_camo_shield_block");
    }

    @Override
    protected void initTE() {
//        GameRegistry.registerTileEntity(NoTickShieldSolidBlockTileEntity.class, RFTools.MODID + "_" + getRegistryName());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new NoTickShieldSolidBlockTileEntity();
    }
}
