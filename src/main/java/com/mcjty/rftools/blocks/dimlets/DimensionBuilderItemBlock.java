package com.mcjty.rftools.blocks.dimlets;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class DimensionBuilderItemBlock extends ItemBlock {

    public DimensionBuilderItemBlock(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int energy = tagCompound.getInteger("Energy");
            list.add(EnumChatFormatting.GREEN + "Energy: " + energy + " rf");
        }
    }
}
