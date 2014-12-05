package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PearlInjectorBlock extends GenericContainerBlock {

    public PearlInjectorBlock() {
        super(Material.iron, PearlInjectorTileEntity.class);
        setBlockName("pearlInjectorBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public String getIdentifyingIconName() {
        return "machinePearlInjector";
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_PEARL_INJECTOR;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        PearlInjectorTileEntity pearlInjectorTileEntity = (PearlInjectorTileEntity) tileEntity;
        PearlInjectorContainer pearlInjectorContainer = new PearlInjectorContainer(entityPlayer, pearlInjectorTileEntity);
        return new GuiPearlInjector(pearlInjectorTileEntity, pearlInjectorContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new PearlInjectorContainer(entityPlayer, (PearlInjectorTileEntity) tileEntity);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        PearlInjectorTileEntity pearlInjectorTileEntity = (PearlInjectorTileEntity)world.getTileEntity(x, y, z);

        if (pearlInjectorTileEntity != null) {
            BlockTools.emptyInventoryInWorld(world, x, y, z, block, pearlInjectorTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        PearlInjectorTileEntity pearlInjectorTileEntity = (PearlInjectorTileEntity)world.getTileEntity(x, y, z);

        if (pearlInjectorTileEntity != null) {
            for (int i = 0 ; i < pearlInjectorTileEntity.getSizeInventory() ; i++) {
                pearlInjectorTileEntity.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        checkRedstone(world, x, y, z);
    }

}
