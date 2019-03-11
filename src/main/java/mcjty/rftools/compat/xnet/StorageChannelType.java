package mcjty.rftools.compat.xnet;

import mcjty.rftools.api.storage.IStorageScanner;
import mcjty.xnet.api.channels.IChannelSettings;
import mcjty.xnet.api.channels.IChannelType;
import mcjty.xnet.api.channels.IConnectorSettings;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StorageChannelType implements IChannelType {

    @Override
    public String getID() {
        return "rftools.storage";
    }

    @Override
    public String getName() {
        return "Storage";
    }

    @Override
    public boolean supportsBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null) {
            return false;
        }
        if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            return true;
        }
        if (te instanceof IInventory) {
            return true;
        }
        if (te instanceof IStorageScanner) {
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public IConnectorSettings createConnector(@Nonnull EnumFacing side) {
        return new StorageConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new StorageChannelSettings();
    }
}
