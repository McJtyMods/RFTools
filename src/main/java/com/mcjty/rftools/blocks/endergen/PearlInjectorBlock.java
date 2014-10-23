package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class PearlInjectorBlock extends GenericContainerBlock {

    public PearlInjectorBlock(Material material) {
        super(material, PearlInjectorTileEntity.class);
        setBlockName("pearlInjectorBlock");
    }

    @Override
    public String getFrontIconName() {
        return "machinePearlInjector";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_PEARL_INJECTOR;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        meta = BlockTools.setRedstoneSignal(meta, powered);
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }


}
