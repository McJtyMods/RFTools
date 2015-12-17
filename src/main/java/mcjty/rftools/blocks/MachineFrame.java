package mcjty.rftools.blocks;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class MachineFrame extends Block {
    public MachineFrame() {
        super(Material.iron);
        setUnlocalizedName("machineFrame");
//        setBlockTextureName(RFTools.MODID + ":" + "machineSide");
        setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(this, "machineFrame");
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return false;
    }
}
