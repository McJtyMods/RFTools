package mcjty.rftools.blocks.storagemonitor;


import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.blocks.storage.ModularStorageContainer;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PacketGetInventoryInfo implements IMessage {

    private int id;
    private BlockPos pos;
    private boolean doscan;

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        pos = NetworkTools.readPos(byteBuf);
        id = byteBuf.readInt();
        doscan = byteBuf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        NetworkTools.writePos(byteBuf, pos);
        byteBuf.writeInt(id);
        byteBuf.writeBoolean(doscan);
    }

    public PacketGetInventoryInfo() {
    }

    public PacketGetInventoryInfo(int worldId, BlockPos pos, boolean doscan) {
        this.id = worldId;
        this.pos = pos;
        this.doscan = doscan;
    }

    public static class Handler implements IMessageHandler<PacketGetInventoryInfo, IMessage> {
        @Override
        public IMessage onMessage(PacketGetInventoryInfo message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(()
                    -> message.onMessageServer(ctx.getServerHandler().player)
                    .ifPresent(p -> sendReplyToClient(p, ctx.getServerHandler().player)));
            return null;
        }

        private void sendReplyToClient(List<PacketReturnInventoryInfo.InventoryInfo> reply, EntityPlayerMP player) {
            PacketReturnInventoryInfo msg = new PacketReturnInventoryInfo(reply);
            RFToolsMessages.INSTANCE.sendTo(msg, player);
        }
    }

    public Optional<List<PacketReturnInventoryInfo.InventoryInfo>> onMessageServer(EntityPlayerMP entityPlayerMP) {
        World world = DimensionManager.getWorld(id);
        if (world == null) {
            return Optional.empty();
        }

        if (!WorldTools.chunkLoaded(world, pos)) {
            return Optional.empty();
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof StorageScannerTileEntity) {
            StorageScannerTileEntity scannerTileEntity = (StorageScannerTileEntity) te;
            Stream<BlockPos> inventories;
            if (doscan) {
                inventories = scannerTileEntity.findInventories();
            } else {
                inventories = scannerTileEntity.getAllInventories();
            }

            List<PacketReturnInventoryInfo.InventoryInfo> invs = inventories
                    .map(pos -> toInventoryInfo(world, pos, scannerTileEntity))
                    .collect(Collectors.toList());

            return Optional.of(invs);
        }

        return Optional.empty();
    }

    private static PacketReturnInventoryInfo.InventoryInfo toInventoryInfo(World world, BlockPos pos, StorageScannerTileEntity te) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        String displayName;
        if (!WorldTools.chunkLoaded(world, pos)) {
            displayName = "[UNLOADED]";
            block = null;
        } else if (world.isAirBlock(pos)) {
            displayName = "[REMOVED]";
            block = null;
        } else {
            displayName = BlockTools.getReadableName(world, pos);
            TileEntity storageTe = world.getTileEntity(pos);
            if (storageTe instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storageTileEntity = (ModularStorageTileEntity) storageTe;
                ItemStack storageModule = storageTileEntity.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
                if (!storageModule.isEmpty()) {
                    if (storageModule.getTagCompound().hasKey("display")) {
                        displayName = storageModule.getDisplayName();
                    }
                }
            }
        }
        return new PacketReturnInventoryInfo.InventoryInfo(pos, displayName, te.isRoutable(pos), block);
    }

}
