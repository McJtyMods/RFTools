package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.Infusable;
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

public class DimensionEditorBlock extends GenericContainerBlock implements Infusable {

    private IIcon iconFront_empty;
    private IIcon iconFront_busy1;
    private IIcon iconFront_busy2;

    public DimensionEditorBlock() {
        super(Material.iron, DimensionEditorTileEntity.class);
        setBlockName("dimensionEditorBlock");
        setHorizRotation(true);
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMENSION_EDITOR;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        super.registerBlockIcons(iconRegister);
        iconFront_empty = iconRegister.registerIcon(RFTools.MODID + ":" + "machineDimensionEditor_empty");
        iconFront_busy1 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineDimensionEditor_busy1");
        iconFront_busy2 = iconRegister.registerIcon(RFTools.MODID + ":" + "machineDimensionEditor_busy2");
    }


    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        DimensionEditorTileEntity dimensionEditorTileEntity = (DimensionEditorTileEntity)world.getTileEntity(x, y, z);

        if (dimensionEditorTileEntity != null) {
            BlockTools.emptyInventoryInWorld(world, x, y, z, block, dimensionEditorTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        DimensionEditorTileEntity dimensionEditorTileEntity = (DimensionEditorTileEntity)world.getTileEntity(x, y, z);

        if (dimensionEditorTileEntity != null) {
            for (int i = 0 ; i < dimensionEditorTileEntity.getSizeInventory() ; i++) {
                dimensionEditorTileEntity.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimensionEditorTileEntity dimensionEditorTileEntity = (DimensionEditorTileEntity) tileEntity;
        DimensionEditorContainer dimensionEditorContainer = new DimensionEditorContainer(entityPlayer, dimensionEditorTileEntity);
        return new GuiDimensionEditor(dimensionEditorTileEntity, dimensionEditorContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new DimensionEditorContainer(entityPlayer, (DimensionEditorTileEntity) tileEntity);
    }


    @Override
    public String getIdentifyingIconName() {
        return "machineDimensionEditor";
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
