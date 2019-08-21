package mcjty.rftools.items;

import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;


import org.lwjgl.input.Keyboard;

import java.util.List;

public class PeaceEssenceItem extends GenericRFToolsItem {
    public PeaceEssenceItem() {
        super("peace_essence");
        setMaxStackSize(64);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<ITextComponent> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(TextFormatting.WHITE + "This essence item is the main ingredient for");
            list.add(TextFormatting.WHITE + "the peaceful dimlet in the Dimlet Workbench.");
            list.add(TextFormatting.WHITE + "Getting this essence is somewhat harder though.");
        } else {
            list.add(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE);
        }
    }
}
