package mcjty.rftools.items.envmodules;

import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.rftools.blocks.environmental.modules.FeatherFallingPlusEModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;



import java.util.List;

public class FeatherFallingPlusEModuleItem extends GenericRFToolsItem implements EnvModuleProvider {

    public FeatherFallingPlusEModuleItem() {
        super("featherfallingplus_module");
        setMaxStackSize(16);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add("This module gives feather falling bonus");
        list.add("when used in the environmental controller.");
        list.add(TextFormatting.GOLD + "Damage will be reduced to zero.");
        list.add(TextFormatting.GREEN + "Uses " + EnvironmentalConfiguration.FEATHERFALLINGPLUS_RFPERTICK.get() + " RF/tick (per cubic block)");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends EnvironmentModule> getServerEnvironmentModule() {
        return FeatherFallingPlusEModule.class;
    }

    @Override
    public String getName() {
        return "Feather+";
    }
}