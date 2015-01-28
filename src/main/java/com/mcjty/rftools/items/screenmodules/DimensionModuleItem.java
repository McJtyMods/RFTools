package com.mcjty.rftools.items.screenmodules;

import com.mcjty.rftools.blocks.screens.ModuleProvider;
import com.mcjty.rftools.blocks.screens.modules.DimensionScreenModule;
import com.mcjty.rftools.blocks.screens.modules.ScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.DimensionClientScreenModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class DimensionModuleItem extends Item implements ModuleProvider {

    public DimensionModuleItem() {
        setMaxStackSize(16);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends ScreenModule> getServerScreenModule() {
        return DimensionScreenModule.class;
    }

    @Override
    public Class<? extends ClientScreenModule> getClientScreenModule() {
        return DimensionClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Dim";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(EnumChatFormatting.GREEN + "Uses " + DimensionScreenModule.RFPERTICK + " RF/tick");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.YELLOW + "Label: " + tagCompound.getString("text"));
            list.add(EnumChatFormatting.YELLOW + "Dimension: " + tagCompound.getInteger("dim"));
        }
    }
}