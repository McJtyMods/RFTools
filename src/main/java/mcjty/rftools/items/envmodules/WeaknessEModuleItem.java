package mcjty.rftools.items.envmodules;

import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.rftools.blocks.environmental.modules.WeaknessEModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;



import java.util.List;

public class WeaknessEModuleItem extends GenericRFToolsItem implements EnvModuleProvider {

    public WeaknessEModuleItem() {
        super("weakness_module");
        setMaxStackSize(16);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add("This module gives weakness when");
        list.add("used in the environmental controller.");
        list.add(TextFormatting.GREEN + "Uses " + EnvironmentalConfiguration.WEAKNESS_RFPERTICK.get() + " RF/tick (per cubic block)");
        if (!EnvironmentalConfiguration.weaknessAvailable.get()) {
            list.add(TextFormatting.RED + "This module only works on mobs (see config)");
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends EnvironmentModule> getServerEnvironmentModule() {
        return WeaknessEModule.class;
    }

    @Override
    public String getName() {
        return "Weakness";
    }
}