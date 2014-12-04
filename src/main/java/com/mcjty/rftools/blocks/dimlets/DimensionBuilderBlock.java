package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class DimensionBuilderBlock extends GenericContainerBlock {

    private IIcon iconFront_empty;
    private IIcon iconFront_busy1;
    private IIcon iconFront_busy2;

    public DimensionBuilderBlock(Material material) {
        super(material, DimensionBuilderTileEntity.class);
        setBlockName("dimensionBuilderBlock");
        setHorizRotation(true);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMENSION_BUILDER;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        super.registerBlockIcons(iconRegister);
        iconFront_empty = iconRegister.registerIcon(RFTools.MODID + ":" + "machineDimensionBuilder_empty");
        iconFront_busy1 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineDimensionBuilder_busy1");
        iconFront_busy2 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineDimensionBuilder_busy2");
    }


    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        DimensionBuilderTileEntity dimensionBuilderTileEntity = (DimensionBuilderTileEntity)world.getTileEntity(x, y, z);

        if (dimensionBuilderTileEntity != null) {
            BlockTools.emptyInventoryInWorld(world, x, y, z, block, dimensionBuilderTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        DimensionBuilderTileEntity dimensionBuilderTileEntity = (DimensionBuilderTileEntity)world.getTileEntity(x, y, z);

        if (dimensionBuilderTileEntity != null) {
            for (int i = 0 ; i < dimensionBuilderTileEntity.getSizeInventory() ; i++) {
                dimensionBuilderTileEntity.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimensionBuilderTileEntity dimensionBuilderTileEntity = (DimensionBuilderTileEntity) tileEntity;
        DimensionBuilderContainer dimensionBuilderContainer = new DimensionBuilderContainer(entityPlayer, dimensionBuilderTileEntity);
        return new GuiDimensionBuilder(dimensionBuilderTileEntity, dimensionBuilderContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new DimensionBuilderContainer(entityPlayer, (DimensionBuilderTileEntity) tileEntity);
    }


    @Override
    public String getIdentifyingIconName() {
        return "machineDimensionBuilder";
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int state = BlockTools.getState(meta);
        if (state == 0) {
            return 10;
        } else {
            return getLightValue();
        }
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public int getMixedBrightnessForBlock(IBlockAccess world, int x, int y, int z) {
//        int meta = world.getBlockMetadata(x, y, z);
//        int state = BlockTools.getState(meta);
//        if (state == 0) {
//            return 13;
//        } else {
//            return super.getMixedBrightnessForBlock(world, x, y, z);
//        }
//    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        if (side == k.ordinal()) {
            int state = BlockTools.getState(meta);
            switch (state) {
                case 0: return iconInd;
                case 1: return iconFront_empty;
                case 2: return iconFront_busy1;
                case 3: return iconFront_busy2;
                default: return iconInd;
            }
        } else {
            return iconSide;
        }
    }

}
