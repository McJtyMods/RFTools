package mcjty.rftools.items.netmonitor;

import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class NetworkMonitorItem extends Item {

    public NetworkMonitorItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        if (world.isRemote) {
            GuiNetworkMonitor.setSelected(x, y, z);
            player.openGui(RFTools.instance, RFTools.GUI_LIST_BLOCKS, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
            return true;
        }
        return true;
    }
}