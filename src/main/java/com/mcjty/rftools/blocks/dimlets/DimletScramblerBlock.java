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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class DimletScramblerBlock extends GenericContainerBlock implements Infusable {

    public DimletScramblerBlock() {
        super(Material.iron, DimletScramblerTileEntity.class);
        setBlockName("dimletScramblerBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_DIMLET_SCRAMBLER;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        DimletScramblerTileEntity DimletScramblerTileEntity = (DimletScramblerTileEntity)world.getTileEntity(x, y, z);

        if (DimletScramblerTileEntity != null) {
            BlockTools.emptyInventoryInWorld(world, x, y, z, block, DimletScramblerTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        DimletScramblerTileEntity DimletScramblerTileEntity = (DimletScramblerTileEntity)world.getTileEntity(x, y, z);

        if (DimletScramblerTileEntity != null) {
            for (int i = 0 ; i < DimletScramblerTileEntity.getSizeInventory() ; i++) {
                DimletScramblerTileEntity.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimletScramblerTileEntity dimletScramblerTileEntity = (DimletScramblerTileEntity) tileEntity;
        DimletScramblerContainer dimletScramblerContainer = new DimletScramblerContainer(entityPlayer, dimletScramblerTileEntity);
        return new GuiDimletScrambler(dimletScramblerTileEntity, dimletScramblerContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new DimletScramblerContainer(entityPlayer, (DimletScramblerTileEntity) tileEntity);
    }


    @Override
    public String getIdentifyingIconName() {
        return "machineDimletScrambler";
    }
}
