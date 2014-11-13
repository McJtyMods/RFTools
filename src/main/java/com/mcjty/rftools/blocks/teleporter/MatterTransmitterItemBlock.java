package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class MatterTransmitterItemBlock extends ItemBlock {

    public MatterTransmitterItemBlock(Block block) {
        super(block);
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            int energy = tagCompound.getInteger("Energy");
            list.add(EnumChatFormatting.GREEN + "Energy: " + energy + " rf");
            String name = tagCompound.getString("tpName");
            list.add(EnumChatFormatting.GREEN + "Name: " + name);
            Coordinate c = Coordinate.readFromNBT(tagCompound, "dest");
            if (c != null && c.getY() >= 0) {
                list.add(EnumChatFormatting.YELLOW + "[DIALED]");
            }
        }
    }
}
