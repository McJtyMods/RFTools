package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.blocks.storage.ModularStorageBlock;
import mcjty.rftools.blocks.storage.ModularStorageContainer;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InventoriesInfoPacketServer implements InfoPacketServer {

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

    public InventoriesInfoPacketServer() {
    }

    public InventoriesInfoPacketServer(int worldId, BlockPos pos, boolean doscan) {
        this.id = worldId;
        this.pos = pos;
        this.doscan = doscan;
    }

    @Override
    public Optional<InfoPacketClient> onMessageServer(EntityPlayerMP entityPlayerMP) {
        World world = DimensionManager.getWorld(id);
        if (world == null) {
            return Optional.empty();
        }

        if (!RFToolsTools.chunkLoaded(world, pos)) {
            return Optional.empty();
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof StorageScannerTileEntity) {
            StorageScannerTileEntity scannerTileEntity = (StorageScannerTileEntity) te;
            List<BlockPos> inventories;
            if (doscan) {
                inventories = scannerTileEntity.findInventories();
            } else {
                inventories = scannerTileEntity.getInventories();
            }

            List<InventoriesInfoPacketClient.InventoryInfo> invs = inventories.stream()
                    .map(pos -> toInventoryInfo(world, pos, scannerTileEntity))
                    .collect(Collectors.toList());

            return Optional.of(new InventoriesInfoPacketClient(invs));
        }

        return Optional.empty();
    }

    private static InventoriesInfoPacketClient.InventoryInfo toInventoryInfo(World world, BlockPos pos, StorageScannerTileEntity te) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        String displayName;
        if (!RFToolsTools.chunkLoaded(world, pos)) {
            displayName = "[UNLOADED]";
            block = null;
        } else if (world.isAirBlock(pos)) {
            displayName = "[REMOVED]";
            block = null;
        } else {
            displayName = BlockInfo.getReadableName(state);
            TileEntity storageTe = world.getTileEntity(pos);
            if (storageTe instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storageTileEntity = (ModularStorageTileEntity) storageTe;
                ItemStack storageModule = storageTileEntity.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
                if (ItemStackTools.isValid(storageModule)) {
                    if (storageModule.getTagCompound().hasKey("display")) {
                        displayName = storageModule.getDisplayName();
                    }
                }
            }
        }
        return new InventoriesInfoPacketClient.InventoryInfo(pos, displayName, te.isRoutable(pos), block);
    }
}
