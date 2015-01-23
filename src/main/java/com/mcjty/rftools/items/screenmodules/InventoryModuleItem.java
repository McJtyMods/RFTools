package com.mcjty.rftools.items.screenmodules;

import com.mcjty.rftools.blocks.screens.ModuleProvider;
import com.mcjty.rftools.blocks.screens.modules.ItemStackScreenModule;
import com.mcjty.rftools.blocks.screens.modules.ScreenModule;
import com.mcjty.rftools.blocks.screens.modules.TextScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.ItemStackClientScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.TextClientScreenModule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InventoryModuleItem extends Item implements ModuleProvider {

    public InventoryModuleItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends ScreenModule> getServerScreenModule() {
        return ItemStackScreenModule.class;
    }

    @Override
    public Class<? extends ClientScreenModule> getClientScreenModule() {
        return ItemStackClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Inventory";
    }
}