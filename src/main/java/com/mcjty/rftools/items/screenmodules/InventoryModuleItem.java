package com.mcjty.rftools.items.screenmodules;

import com.mcjty.rftools.blocks.screens.ModuleProvider;
import com.mcjty.rftools.blocks.screens.modules.ItemStackScreenModule;
import com.mcjty.rftools.blocks.screens.modules.ScreenModule;
import com.mcjty.rftools.blocks.screens.modules.TextScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.ItemStackClientScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.TextClientScreenModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class InventoryModuleItem extends Item implements ModuleProvider {

    public InventoryModuleItem() {
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(EnumChatFormatting.GREEN + "Uses " + ItemStackScreenModule.RFPERTICK + " RF/tick");
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
        return "Inv";
    }
}