package mcjty.rftools.compat.xnet;

public class StorageChannelType {} /* @todo 1.14 (xnet) implements IChannelType {

    @Override
    public String getID() {
        return "rftools.storage";
    }

    @Override
    public String getName() {
        return "Storage";
    }

    @Override
    public boolean supportsBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Direction side) {
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
    public IConnectorSettings createConnector(@Nonnull Direction side) {
        return new StorageConnectorSettings(side);
    }

    @Nonnull
    @Override
    public IChannelSettings createChannel() {
        return new StorageChannelSettings();
    }
}
*/