package mcjty.rftools.items.envmodules;

import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.rftools.blocks.environmental.modules.NoTeleportEModule;
import mcjty.rftools.blocks.environmental.modules.PeacefulEModule;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class NoTeleportEModuleItem extends GenericRFToolsItem implements EnvModuleProvider {

    public NoTeleportEModuleItem() {
        super("noteleport_module");
        setMaxStackSize(16);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add("This module prevents enderman from teleporting");
        list.add("when used in the environmental controller.");
        list.add(TextFormatting.GREEN + "Uses " + EnvironmentalConfiguration.NOTELEPORT_RFPERTICK + " RF/tick (per cubic block)");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends EnvironmentModule> getServerEnvironmentModule() {
        return NoTeleportEModule.class;
    }

    @Override
    public String getName() {
        return "NoTP";
    }
}