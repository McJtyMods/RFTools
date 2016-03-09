package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class NoTickSolidShieldBlock extends SolidShieldBlock {

    @Override
    protected void init() {
        setRegistryName("notick_solid_shield_block");
        setUnlocalizedName("notick_solid_shield_block");
    }

    @Override
    protected void initTE() {
        GameRegistry.registerTileEntity(NoTickShieldBlockTileEntity.class, RFTools.MODID + "_" + getRegistryName());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new NoTickShieldBlockTileEntity();
    }
}
