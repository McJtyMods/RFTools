package mcjty.rftools.items.powercell;

import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
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
        list.add(TextFormatting.WHITE + "Use to connect a powercell multiblock");
        int id = getId(itemStack);
        if (id == -1) {
            list.add(TextFormatting.YELLOW + "[UNLINKED]");
        } else {
            list.add(TextFormatting.GREEN + "Link id: " + id);
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

    public static void setId(ItemStack stack, int id) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger("id", id);
    }
}
