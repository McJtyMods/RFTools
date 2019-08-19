package mcjty.rftools.items.netmonitor;

import mcjty.rftools.RFTools;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
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
    public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            GuiNetworkMonitor.setSelected(pos);
            player.openGui(RFTools.instance, GuiProxy.GUI_LIST_BLOCKS, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.SUCCESS;
    }
}