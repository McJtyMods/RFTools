package com.mcjty.rftools.items.manual;

import com.mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RFToolsManualDimensionItem extends ItemBook {

    public RFToolsManualDimensionItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_MANUAL_DIMENSION, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return stack;
        }
        return stack;
    }

}
