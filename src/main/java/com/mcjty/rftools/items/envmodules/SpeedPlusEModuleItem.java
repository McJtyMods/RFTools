package com.mcjty.rftools.items.envmodules;

import com.mcjty.rftools.blocks.environmental.EnvModuleProvider;
import com.mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import com.mcjty.rftools.blocks.environmental.modules.SpeedPlusEModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class SpeedPlusEModuleItem extends Item implements EnvModuleProvider {

    public SpeedPlusEModuleItem() {
        setMaxStackSize(16);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add("This module gives speed III bonus when");
        list.add("used in the environmental controller.");
        list.add(EnumChatFormatting.GREEN + "Uses " + SpeedPlusEModule.RFPERTICK + " RF/tick (per cubic block)");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends EnvironmentModule> getServerEnvironmentModule() {
        return SpeedPlusEModule.class;
    }

    @Override
    public String getName() {
        return "Speed+";
    }
}