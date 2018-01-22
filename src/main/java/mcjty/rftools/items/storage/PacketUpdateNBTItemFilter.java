package mcjty.rftools.items.storage;

import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketUpdateNBTItem;
import mcjty.lib.network.PacketUpdateNBTItemHandler;
import net.minecraft.item.ItemStack;

public class PacketUpdateNBTItemFilter extends PacketUpdateNBTItem {

    public PacketUpdateNBTItemFilter() {
    }

    public PacketUpdateNBTItemFilter(Argument... arguments) {
        super(arguments);
    }

    @Override
    protected boolean isValidItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof StorageFilterItem;
    }

    public static class Handler extends PacketUpdateNBTItemHandler<PacketUpdateNBTItemFilter> {

    }
}
