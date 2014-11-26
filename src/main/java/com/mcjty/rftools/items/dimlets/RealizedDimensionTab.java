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
            String name = tagCompound.getString("name");
            if (name != null) {
                list.add(EnumChatFormatting.BLUE + "Name: " + name);
            }
            for (DimletType type : DimletType.values()) {
                if (tagCompound.hasKey(type.getName())) {
                    NBTTagIntArray tagIntArray = (NBTTagIntArray) tagCompound.getTag(type.getName());
                    if (tagIntArray != null) {
                        int length = tagIntArray.func_150302_c().length;
                        if (length > 0) {
                            list.add(EnumChatFormatting.GREEN + type.getName() + " " + length + " dimlets");
                        }
                    }
                }
            }
            Integer pct = tagCompound.getInteger("pct");
            if (pct == 100) {
                list.add(EnumChatFormatting.BLUE + "Dimension ready!");
                int maintainCost = tagCompound.getInteger("rfMaintainCost");
                list.add(EnumChatFormatting.YELLOW + "    Maintenance cost: " + maintainCost + " RF/tick");
            } else {
                int createCost = tagCompound.getInteger("rfCreateCost");
                int maintainCost = tagCompound.getInteger("rfMaintainCost");
                int tickCost = tagCompound.getInteger("tickCost");
                list.add(EnumChatFormatting.BLUE + "Dimension progress: " + pct + "%");
                list.add(EnumChatFormatting.YELLOW + "    Creation cost: " + createCost + " RF/tick");
                list.add(EnumChatFormatting.YELLOW + "    Maintenance cost: " + maintainCost + " RF/tick");
                list.add(EnumChatFormatting.YELLOW + "    Tick cost: " + tickCost + " ticks");
            }
        }
    }
}
