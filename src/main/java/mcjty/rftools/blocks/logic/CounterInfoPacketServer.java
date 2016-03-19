package mcjty.rftools.blocks.logic;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Optional;

public class CounterInfoPacketServer implements InfoPacketServer {

    private int dimension;
    private BlockPos pos;

    public CounterInfoPacketServer() {
    }

    public CounterInfoPacketServer(int dimension, BlockPos pos) {
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
        if (world != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof CounterTileEntity) {
                CounterTileEntity tileEntity = (CounterTileEntity) te;
                cnt = tileEntity.getCurrent();
            }
        }
        return Optional.of(new CounterInfoPacketClient(cnt));
    }
}
