package com.mcjty.rftools.items.dimlets;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RealizedDimensionTab extends Item {

    public RealizedDimensionTab() {
        setMaxStackSize(1);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            for (DimletType type : DimletType.values()) {
                NBTTagIntArray tagIntArray = (NBTTagIntArray) tagCompound.getTag(type.getName());
                if (tagIntArray != null) {
                    list.add(EnumChatFormatting.GREEN + type.getName() + " " + tagIntArray.func_150302_c().length + " dimlets");
                }
            }
        }
    }
}
