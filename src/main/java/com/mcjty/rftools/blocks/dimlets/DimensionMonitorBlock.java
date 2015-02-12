package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.EmptyContainer;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.logic.LogicSlabBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class DimensionMonitorBlock extends LogicSlabBlock {
    public DimensionMonitorBlock() {
        super(Material.iron, "dimensionMonitorBlock", DimensionMonitorTileEntity.class);
        setCreativeTab(RFTools.tabRfTools);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int level = tagCompound.getInteger("level");
            list.add(EnumChatFormatting.GREEN + "Level: " + level);
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            list.add(EnumChatFormatting.WHITE + "Send out a redstone signal in case the power");
            list.add(EnumChatFormatting.WHITE + "level of the current dimension goes below");
            list.add(EnumChatFormatting.WHITE + "some thresshold.");
        } else {
            list.add(EnumChatFormatting.WHITE + RFTools.SHIFT_MESSAGE);
        }
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_TIMER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        DimensionMonitorTileEntity dimensionMonitorTileEntity = (DimensionMonitorTileEntity) tileEntity;
        return new GuiDimensionMonitor(dimensionMonitorTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineDimMonitorTop";
    }

}
