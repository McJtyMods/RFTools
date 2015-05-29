package mcjty.rftools.items.screenmodules;

import mcjty.rftools.blocks.screens.ModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ClockScreenModule;
import mcjty.rftools.blocks.screens.modules.ScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ClockClientScreenModule;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ClockModuleItem extends Item implements ModuleProvider {

    public ClockModuleItem() {
        setMaxStackSize(16);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(EnumChatFormatting.GREEN + "Uses " + ScreenConfiguration.CLOCK_RFPERTICK + " RF/tick");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends ScreenModule> getServerScreenModule() {
        return ClockScreenModule.class;
    }

    @Override
    public Class<? extends ClientScreenModule> getClientScreenModule() {
        return ClockClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Clock";
    }
}