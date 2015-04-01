package mcjty.rftools.blocks;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class MachineFrame extends Block {
    public MachineFrame() {
        super(Material.iron);
        setBlockName("machineFrame");
        setBlockTextureName(RFTools.MODID + ":" + "machineSide");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return false;
    }
}
