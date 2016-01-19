package mcjty.rftools.items.manual;

import mcjty.rftools.RFTools;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RFToolsManualItem extends GenericRFToolsItem {

    public RFToolsManualItem() {
        super("rftools_manual");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) {
            player.openGui(RFTools.instance, RFTools.GUI_MANUAL_MAIN, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return stack;
        }
        return stack;
    }

}
