package com.mcjty.rftools.items.screenmodules;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TextModuleItem extends Item {

    public TextModuleItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

}