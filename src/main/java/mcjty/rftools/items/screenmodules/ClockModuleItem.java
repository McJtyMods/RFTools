package mcjty.rftools.items.screenmodules;

import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ClockScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ClockClientScreenModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ClockModuleItem extends Item implements IModuleProvider {

    public ClockModuleItem() {
        super(new Properties()
                .maxStackSize(16)
                .defaultMaxDamage(1)
                .group(RFTools.setup.getTab()));
        setRegistryName("clock_module");
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag advanced) {
        super.addInformation(itemStack, world, list, advanced);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.CLOCK_RFPERTICK.get() + " RF/tick"));
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public Class<ClockScreenModule> getServerScreenModule() {
        return ClockScreenModule.class;
    }

    @Override
    public Class<ClockClientScreenModule> getClientScreenModule() {
        return ClockClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Clock";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder.
                label("Label:").text("text", "Label text").color("color", "Label color").nl().
                toggle("large", "Large", "Large or small font").nl();
    }
}