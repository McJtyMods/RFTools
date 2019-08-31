package mcjty.rftools.items.netmonitor;

import mcjty.rftools.RFTools;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NetworkMonitorItem extends Item {

    public NetworkMonitorItem() {
        super(new Properties().maxStackSize(1).defaultMaxDamage(1).group(RFTools.setup.getTab()));
        setRegistryName("network_monitor");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote) {
            BlockPos pos = context.getPos();
            PlayerEntity player = context.getPlayer();
            GuiNetworkMonitor.setSelected(pos);
            // @todo 1.14
//            player.openGui(RFTools.instance, GuiProxy.GUI_LIST_BLOCKS, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.SUCCESS;
    }
}