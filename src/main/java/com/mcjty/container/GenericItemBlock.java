package com.mcjty.container;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.List;

public class GenericItemBlock extends ItemBlock {
    private final GenericBlock genericBlock;

    public GenericItemBlock(Block block) {
        super(block);
        genericBlock = (GenericBlock) block;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        genericBlock.addInformation(itemStack, player, list, whatIsThis);
    }

}
