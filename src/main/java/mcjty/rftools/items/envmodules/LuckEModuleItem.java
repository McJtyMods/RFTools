package mcjty.rftools.items.envmodules;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.EnvironmentalConfiguration;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.rftools.blocks.environmental.modules.LuckEModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class LuckEModuleItem extends Item implements EnvModuleProvider {

    public LuckEModuleItem() {
        super(new Properties().maxStackSize(16).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("luck_module");
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        list.add(new StringTextComponent("This module gives luck bonus when"));
        list.add(new StringTextComponent("used in the environmental controller."));
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + EnvironmentalConfiguration.LUCK_RFPERTICK.get() + " RF/tick (per cubic block)"));
    }

    @Override
    public Class<? extends EnvironmentModule> getServerEnvironmentModule() {
        return LuckEModule.class;
    }

    @Override
    public String getModuleName() {
        return "Luck";
    }
}