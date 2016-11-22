package mcjty.rftools.items.netmonitor;

import mcjty.rftools.RFTools;
import mcjty.rftools.items.GenericRFToolsItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetworkMonitorItem extends GenericRFToolsItem {

    public NetworkMonitorItem() {
        super("network_monitor");
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    protected EnumActionResult clOnItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            GuiNetworkMonitor.setSelected(pos);
            player.openGui(RFTools.instance, RFTools.GUI_LIST_BLOCKS, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.SUCCESS;
    }
}