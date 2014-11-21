package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class DimensionEnscriberBlock extends GenericContainerBlock {

    public DimensionEnscriberBlock(Material material) {
        super(material, DimensionEnscriberTileEntity.class);
        setBlockName("dimensionEnscriberBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMENSION_ENSCRIBER;
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDimensionEnscriber";
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        DimensionEnscriberTileEntity dimensionEnscriberTileEntity = (DimensionEnscriberTileEntity)world.getTileEntity(x, y, z);

        if (dimensionEnscriberTileEntity != null) {
            BlockTools.emptyInventoryInWorld(world, x, y, z, block, dimensionEnscriberTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        DimensionEnscriberTileEntity dimensionEnscriberTileEntity = (DimensionEnscriberTileEntity)world.getTileEntity(x, y, z);

        if (dimensionEnscriberTileEntity != null) {
            for (int i = 0 ; i < dimensionEnscriberTileEntity.getSizeInventory() ; i++) {
                dimensionEnscriberTileEntity.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimensionEnscriberTileEntity dimensionEnscriberTileEntity = (DimensionEnscriberTileEntity) tileEntity;
        DimensionEnscriberContainer dimensionEnscriberContainer = new DimensionEnscriberContainer(entityPlayer, dimensionEnscriberTileEntity);
        return new GuiDimensionEnscriber(dimensionEnscriberTileEntity, dimensionEnscriberContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new DimensionEnscriberContainer(entityPlayer, (DimensionEnscriberTileEntity) tileEntity);
    }



}
