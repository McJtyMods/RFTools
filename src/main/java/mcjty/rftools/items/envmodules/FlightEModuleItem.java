package mcjty.rftools.items.envmodules;

import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.rftools.blocks.environmental.modules.FlightEModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class FlightEModuleItem extends Item implements EnvModuleProvider {

    public FlightEModuleItem() {
        super(new Item.Properties().maxStackSize(16));
        setRegistryName("flight_module");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, list, flagIn);
        list.add(new StringTextComponent("This module gives creative type flying capabilities"));
        list.add(new StringTextComponent("when used in the environmental controller."));
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + EnvironmentalConfiguration.FLIGHT_RFPERTICK.get() + " RF/tick (per cubic block)"));
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends EnvironmentModule> getServerEnvironmentModule() {
        return FlightEModule.class;
    }

    @Override
    public String getModuleName() {
        return "Flight";
    }
}