package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.RFTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SimpleScreenBlock extends GenericBlock {

    public SimpleScreenBlock() {
        super(Material.iron, SimpleScreenTileEntity.class);
        float width = 0.25F;
        float height = 1.0F;
        this.setBlockBounds(0.5F - width, 0.0F, 0.5F - width, 0.5F + width, height, 0.5F + width);
        setBlockName("simpleScreenBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return 0;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineSide";
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        float f = 0.28125F;
        float f1 = 0.78125F;
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
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(world, x, y, z);
        return super.getSelectedBoundingBoxFromPool(world, x, y, z);
    }

    /**
     * The type of render function that is called for this block
     */
    public int getRenderType() {
        return -1;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock() {
        return false;
    }

    public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    public boolean isOpaqueCube() {
        return false;
    }

    /**
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        boolean flag = false;

        int meta = world.getBlockMetadata(x, y, z);
        flag = true;

        if (meta == 2 && world.getBlock(x, y, z + 1).getMaterial().isSolid()) {
            flag = false;
        }

        if (meta == 3 && world.getBlock(x, y, z - 1).getMaterial().isSolid()) {
            flag = false;
        }

        if (meta == 4 && world.getBlock(x + 1, y, z).getMaterial().isSolid()) {
            flag = false;
        }

        if (meta == 5 && world.getBlock(x - 1, y, z).getMaterial().isSolid()) {
            flag = false;
        }

        if (flag) {
            this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }

        super.onNeighborBlockChange(world, x, y, z, block);
    }
}