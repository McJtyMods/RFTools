package mcjty.rftools.items.screenmodules;

import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.IModuleGuiBuilder;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ComputerScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ComputerClientScreenModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ComputerModuleItem extends Item implements IModuleProvider {

    public ComputerModuleItem() {
        super(new Properties().maxStackSize(16).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("computer_module");
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }
//
    @Override
    public Class<ComputerScreenModule> getServerScreenModule() {
        return ComputerScreenModule.class;
    }

    @Override
    public Class<ComputerClientScreenModule> getClientScreenModule() {
        return ComputerClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Comp";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .leftLabel("Contents of this module is").nl()
                .leftLabel("controlled with a computer.").nl()
                .leftLabel("Only works with OpenComputers.").nl() // "Only works with OC or CC."
                .label("Tag:").text("moduleTag", "Tag used by LUA to identify module").nl();
    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.COMPUTER_RFPERTICK.get() + " RF/tick"));
    }

}
