package mcjty.rftools.blocks.powercell;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Optional;

public class PowerCellInfoPacketServer implements InfoPacketServer {

    private int id;
    private BlockPos pos;

    public PowerCellInfoPacketServer() {
    }

    public PowerCellInfoPacketServer(PowerCellTileEntity tileEntity) {
        this.id = tileEntity.getNetworkId();
        pos = tileEntity.getPos();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        NetworkTools.writePos(buf, pos);
    }

    @Override
    public Optional<InfoPacketClient> onMessageServer(EntityPlayerMP player) {
        World world = player.worldObj;

        TileEntity te = world.getTileEntity(pos);
        if (id == -1) {
            if (te instanceof PowerCellTileEntity) {
                PowerCellTileEntity powerCellTileEntity = (PowerCellTileEntity) te;
                return Optional.of(new PowerCellInfoPacketClient(powerCellTileEntity.getEnergy(), 1, world.getBlockState(pos).getBlock() == PowerCellSetup.advancedPowerCellBlock ? 1 : 0,
                        powerCellTileEntity.getTotalInserted(), powerCellTileEntity.getTotalExtracted()));
            } else {
                return Optional.empty();
            }
        } else {
            PowerCellNetwork generatorNetwork = PowerCellNetwork.getChannels(world);
            PowerCellNetwork.Network network = generatorNetwork.getChannel(id);
            int totInserted = 0;
            int totExtracted = 0;
            if (te instanceof PowerCellTileEntity) {
                PowerCellTileEntity powerCellTileEntity = (PowerCellTileEntity) te;
                totInserted = powerCellTileEntity.getTotalInserted();
                totExtracted = powerCellTileEntity.getTotalExtracted();
            }

            return Optional.of(new PowerCellInfoPacketClient(network.getEnergy(), network.getBlockCount(), network.getAdvancedBlockCount(),
                    totInserted, totExtracted));
        }
    }
}
