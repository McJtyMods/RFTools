package mcjty.rftools.items.powercell;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;



import java.util.List;

public class PowerCellCardItem extends Item {

    public PowerCellCardItem() {
        super("powercell_card");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<ITextComponent> list, ITooltipFlag whatIsThis) {
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
        if (!stack.getTag().hasKey("id")) {
            return -1;
        }
        return stack.getTag().getInteger("id");
    }

    public static void setId(ItemStack stack, int id) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new CompoundNBT());
        }
        stack.getTag().setInteger("id", id);
    }
}
