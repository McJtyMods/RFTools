package mcjty.rftools.blocks;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MachineBase extends Block {
    public MachineBase() {
        super(Material.iron);
        setUnlocalizedName("machine_base");
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
        setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(this, "machine_base");
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return false;
    }
}
