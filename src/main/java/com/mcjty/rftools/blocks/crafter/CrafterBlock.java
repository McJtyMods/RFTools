package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

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
    protected void breakWithWrench(World world, int x, int y, int z) {
        // To avoid the inventory being dropped all over the place when wrenching we clear it first.
        CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3)world.getTileEntity(x, y, z);

        if (crafterBlockTileEntity != null) {
            for (int i = 0 ; i < crafterBlockTileEntity.getSizeInventory() ; i++) {
                crafterBlockTileEntity.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        return onBlockActivatedDefaultWrench(world, x, y, z, player);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

    @Override
    public String getIdentifyingIconName() {
        return frontName;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_CRAFTER;
    }
}
