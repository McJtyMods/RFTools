package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SimpleScreenBlock extends GenericContainerBlock {

    public SimpleScreenBlock() {
        super(Material.iron, SimpleScreenTileEntity.class);
        float width = 0.5F;
        float height = 1.0F;
        this.setBlockBounds(0.5F - width, 0.0F, 0.5F - width, 0.5F + width, height, 0.5F + width);
        setBlockName("simpleScreenBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public String getSideIconName() {
        return "screenFrame_icon";
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        float f = 0.0F;
        float f1 = 1.0F;
        float f2 = 0.0F;
        float f3 = 1.0F;
        float f4 = 0.125F;
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

        if (meta == 2) {
            this.setBlockBounds(f2, f, 1.0F - f4, f3, f1, 1.0F);
        }

        if (meta == 3) {
            this.setBlockBounds(f2, f, 0.0F, f3, f1, f4);
        }

        if (meta == 4) {
            this.setBlockBounds(1.0F - f4, f, f2, 1.0F, f1, f3);
        }

        if (meta == 5) {
            this.setBlockBounds(0.0F, f, f2, f4, f1, f3);
        }
    }

    /**
     * Returns the bounding box of the wired rectangular prism to render.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    /**
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType() {
        return -1;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_SCREEN;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        SimpleScreenTileEntity screenTileEntity = (SimpleScreenTileEntity) tileEntity;
        ScreenContainer screenContainer = new ScreenContainer(entityPlayer, screenTileEntity);
        return new GuiScreen(screenTileEntity, screenContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new ScreenContainer(entityPlayer, (SimpleScreenTileEntity) tileEntity);
    }

}
