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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

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

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
            list.add(EnumChatFormatting.GREEN + "Contents: " + bufferTagList.tagCount() + " stacks");
        }
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
