package mcjty.rftools.items.powercell;

import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class PowerCellCardItem extends GenericRFToolsItem {

    public PowerCellCardItem() {
        super("powercell_card");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        list.add(EnumChatFormatting.WHITE + "Use this module to form a powercell multiblock");
        int id = getId(itemStack);
        if (id == -1) {
            list.add(EnumChatFormatting.YELLOW + "[UNLINKED]");
        } else {
            list.add(EnumChatFormatting.BLUE + "Link id:" + id);
        }
    }

    public static int getId(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return -1;
        }
        if (!stack.getTagCompound().hasKey("id")) {
            return -1;
        }
        return stack.getTagCompound().getInteger("id");
    }

}
