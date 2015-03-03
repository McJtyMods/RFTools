package com.mcjty.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Use this in case you want a container with no slots (for example, for energy storage only).
 */
public class EmptyContainer extends GenericContainer {

    public EmptyContainer(EntityPlayer player) {
        super(EmptyContainerFactory.getInstance(), player);
    }

    @Override
    public void putStackInSlot(int index, ItemStack stack) {
    }
}
