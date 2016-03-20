package mcjty.rftools.items.screenmodules;

import mcjty.rftools.api.screens.IClientScreenModule;
import mcjty.rftools.api.screens.IModuleProvider;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.blocks.screens.ScreenConfiguration;
import mcjty.rftools.blocks.screens.modules.TextScreenModule;
import mcjty.rftools.blocks.screens.modulesclient.TextClientScreenModule;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TextModuleItem extends GenericRFToolsItem implements IModuleProvider {

    public TextModuleItem() {
        super("text_module");
        setMaxStackSize(16);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(TextFormatting.GREEN + "Uses " + ScreenConfiguration.TEXT_RFPERTICK + " RF/tick");
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(TextFormatting.YELLOW + "Text: " + tagCompound.getString("text"));
        }
    }

    @Override
    public Class<? extends IScreenModule> getServerScreenModule() {
        return TextScreenModule.class;
    }

    @Override
    public Class<? extends IClientScreenModule> getClientScreenModule() {
        return TextClientScreenModule.class;
    }

    @Override
    public String getName() {
        return "Text";
    }
}