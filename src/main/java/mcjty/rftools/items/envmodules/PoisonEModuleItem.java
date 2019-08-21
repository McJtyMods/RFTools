package mcjty.rftools.items.envmodules;

import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.rftools.blocks.environmental.modules.PoisonEModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;



import java.util.List;

public class PoisonEModuleItem extends GenericRFToolsItem implements EnvModuleProvider {

    public PoisonEModuleItem() {
        super("poison_module");
        setMaxStackSize(16);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<ITextComponent> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add("This module gives poison when");
        list.add("used in the environmental controller.");
        list.add(TextFormatting.GREEN + "Uses " + EnvironmentalConfiguration.POISON_RFPERTICK.get() + " RF/tick (per cubic block)");
        if (!EnvironmentalConfiguration.poisonAvailable.get()) {
            list.add(TextFormatting.RED + "This module only works on mobs (see config)");
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends EnvironmentModule> getServerEnvironmentModule() {
        return PoisonEModule.class;
    }

    @Override
    public String getModuleName() {
        return "Poison";
    }
}