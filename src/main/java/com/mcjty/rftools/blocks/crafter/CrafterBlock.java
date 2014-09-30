package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CrafterBlock extends GenericContainerBlock {
    private String frontName;

    public CrafterBlock(Material material, String blockName, String frontName, Class<? extends TileEntity> tileEntityClass) {
        super(material, tileEntityClass);
        setBlockName(blockName);
        this.frontName = frontName;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3)world.getTileEntity(x, y, z);

        if (crafterBlockTileEntity != null) {
            // To avoid the ghost items being dropped in the world (which would give easy item duplication)
            // we first clear out the crafting grid here.
            for (int i = CrafterContainerFactory.SLOT_CRAFTINPUT ; i <= CrafterContainerFactory.SLOT_CRAFTOUTPUT ; i++) {
                crafterBlockTileEntity.setInventorySlotContents(i, null);
            }

            BlockTools.emptyInventoryInWorld(world, x, y, z, block, crafterBlockTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);

    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        meta = BlockTools.setRedstoneSignal(meta, powered);
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    public String getFrontIconName() {
        return frontName;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_CRAFTER;
    }
}
