package mcjty.rftools.blocks.storage;

import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketUpdateNBTItem;
import mcjty.lib.network.PacketUpdateNBTItemHandler;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.items.storage.StorageModuleTabletItem;
import net.minecraft.item.ItemStack;

public class PacketUpdateNBTItemStorage extends PacketUpdateNBTItem {

    public PacketUpdateNBTItemStorage() {
    }

    public PacketUpdateNBTItemStorage(Argument... arguments) {
        super(arguments);
    }

    @Override
    protected boolean isValidItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof StorageModuleTabletItem;
    }

    public static class Handler extends PacketUpdateNBTItemHandler<PacketUpdateNBTItemStorage> {

    }
}
