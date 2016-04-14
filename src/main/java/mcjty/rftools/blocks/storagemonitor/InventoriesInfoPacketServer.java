package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.List;
import java.util.Optional;

public class InventoriesInfoPacketServer implements InfoPacketServer {

    private int id;
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        pos = NetworkTools.readPos(byteBuf);
        id = byteBuf.readInt();

    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        NetworkTools.writePos(byteBuf, pos);
        byteBuf.writeInt(id);
    }

    public InventoriesInfoPacketServer(World world, BlockPos pos) {
        this.id = world.provider.getDimension();
        this.pos = pos;
    }

    @Override
    public Optional<InfoPacketClient> onMessageServer(EntityPlayerMP entityPlayerMP) {
        World world = DimensionManager.getWorld(id);
        if (world == null) {
            return Optional.empty();
        }

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof StorageScannerTileEntity) {
            StorageScannerTileEntity scannerTileEntity = (StorageScannerTileEntity) te;
            List<BlockPos> inventories = scannerTileEntity.findInventories();
            return Optional.of(new InventoriesInfoPacketClient(inventories));
        }

        return Optional.empty();
    }
}
