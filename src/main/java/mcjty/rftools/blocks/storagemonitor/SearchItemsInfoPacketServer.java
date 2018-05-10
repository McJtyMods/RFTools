package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.Optional;
import java.util.Set;

public class SearchItemsInfoPacketServer implements InfoPacketServer {

    private int id;
    private BlockPos pos;
    private String text;

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        pos = NetworkTools.readPos(byteBuf);
        id = byteBuf.readInt();
        text = NetworkTools.readString(byteBuf);
    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        NetworkTools.writePos(byteBuf, pos);
        byteBuf.writeInt(id);
        NetworkTools.writeString(byteBuf, text);
    }


    public SearchItemsInfoPacketServer() {
    }

    public SearchItemsInfoPacketServer(int worldId, BlockPos pos, String text) {
        this.id = worldId;
        this.pos = pos;
        this.text = text;
    }

    @Override
    public Optional<InfoPacketClient> onMessageServer(EntityPlayerMP entityPlayerMP) {
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
            Set<BlockPos> inventories = scannerTileEntity.performSearch(text);
            return Optional.of(new SearchItemsInfoPacketClient(inventories));
        }

        return Optional.empty();
    }
}
