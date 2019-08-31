package mcjty.rftools.items;

import mcjty.lib.McJtyLib;
import mcjty.rftools.RFTools;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class PeaceEssenceItem extends Item {
    public PeaceEssenceItem() {
        super(new Properties().maxStackSize(64).group(RFTools.setup.getTab()));
        setRegistryName("peace_essence");
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "This essence item is the main ingredient for"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "the peaceful dimlet in the Dimlet Workbench."));
            list.add(new StringTextComponent(TextFormatting.WHITE + "Getting this essence is somewhat harder though."));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + GuiProxy.SHIFT_MESSAGE));
        }
    }
}
