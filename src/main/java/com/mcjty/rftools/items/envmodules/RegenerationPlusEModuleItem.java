package com.mcjty.rftools.items.envmodules;

import com.mcjty.rftools.blocks.environmental.EnvModuleProvider;
import com.mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import com.mcjty.rftools.blocks.environmental.modules.RegenerationPlusEModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class RegenerationPlusEModuleItem extends Item implements EnvModuleProvider {

    public RegenerationPlusEModuleItem() {
        setMaxStackSize(16);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add("This module gives regeneration III bonus when");
        list.add("used in the environmental controller.");
        list.add(EnumChatFormatting.GREEN + "Uses " + RegenerationPlusEModule.RFPERTICK + " RF/tick (per cubic block)");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends EnvironmentModule> getServerEnvironmentModule() {
        return RegenerationPlusEModule.class;
    }

    @Override
    public String getName() {
        return "Regen+";
    }
}