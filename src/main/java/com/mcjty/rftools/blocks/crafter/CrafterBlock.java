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
        super(material);
        setBlockName("crafterBlock");
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new CrafterBlockTileEntity();
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof CrafterBlockTileEntity)) {
            return true;
        }
        if (world.isRemote) {
            return true;
        }
        player.openGui(RFTools.instance, RFTools.GUI_CRAFTER, world, x, y, z);
        return true;
    }

    @Override
    public String getFrontIconName() {
        return "machineCrafter";
    }
}
