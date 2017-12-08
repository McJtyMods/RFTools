package mcjty.rftools.items.screenmodules;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.ComputerScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.ComputerClientScreenModule;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ComputerModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public ComputerModuleItem() {
        super("computer_module");
        setMaxStackSize(16);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public Class<? extends IScreenModule> getServerScreenModule() {
        return ComputerScreenModule.class;
    }

    @Override
    public Class<? extends IClientScreenModule> getClientScreenModule() {
        return ComputerClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Comp";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void clAddInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.clAddInformation(stack, playerIn, tooltip, advanced);
        tooltip.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.COMPUTER_RFPERTICK + " RF/tick");
    }

}
