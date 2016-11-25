package mcjty.rftools.blocks.storage;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Optional;

public class StorageInfoPacketServer implements InfoPacketServer {

    private int dimension;
    private BlockPos pos;

    public StorageInfoPacketServer() {
    }

    public StorageInfoPacketServer(int dimension, BlockPos pos) {
        this.dimension = dimension;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
        NetworkTools.writePos(buf, pos);
    }

    @Override
    public Optional<InfoPacketClient> onMessageServer(EntityPlayerMP player) {
        WorldServer world = DimensionManager.getWorld(dimension);
        int cnt = -1;
        String nameModule = "";
        if (world != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
                cnt = modularStorageTileEntity.getNumStacks();
                ItemStack storageModule = modularStorageTileEntity.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
                if (ItemStackTools.isValid(storageModule) && storageModule.getTagCompound().hasKey("display")) {
                    nameModule = storageModule.getDisplayName();
                }
            }
        }
        return Optional.of(new StorageInfoPacketClient(cnt, nameModule));
    }
}
