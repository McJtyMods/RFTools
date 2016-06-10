package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class NoTickInvisibleShieldBlock extends InvisibleShieldBlock {

    @Override
    protected void init() {
        setUnlocalizedName("rftools.notick_invisible_shield_block");
        setRegistryName("notick_invisible_shield_block");
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
