package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class CrafterBlock extends GenericContainerBlock {

    public CrafterBlock(Material material) {
        super(material, CrafterBlockTileEntity.class);
        setBlockName("crafterBlock");
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        CrafterBlockTileEntity crafterBlockTileEntity = (CrafterBlockTileEntity)world.getTileEntity(x, y, z);

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
        System.out.print("meta = " + meta);
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        System.out.print(", powered = " + powered);
        meta = BlockTools.setRedstoneSignal(meta, powered);
        System.out.println(", -> meta = " + meta);

        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    public String getFrontIconName() {
        return "machineCrafter";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_CRAFTER;
    }
}
