package mcjty.rftools.blocks.powercell;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
        World world = player.getEntityWorld();

        TileEntity te = world.getTileEntity(pos);
        if (id == -1) {
            if (te instanceof PowerCellTileEntity) {
                PowerCellTileEntity powerCellTileEntity = (PowerCellTileEntity) te;
                return Optional.of(new PowerCellInfoPacketClient(
                        powerCellTileEntity.getEnergy(), 1,
                        PowerCellBlock.isSimple(world.getBlockState(pos).getBlock()) ? 1 : 0,
                        PowerCellBlock.isAdvanced(world.getBlockState(pos).getBlock()) ? 1 : 0,
                        powerCellTileEntity.getTotalInserted(), powerCellTileEntity.getTotalExtracted(),
                        powerCellTileEntity.getRfPerTickPerSide(), 1.0f));
            } else {
                return Optional.empty();
            }
        } else {
            PowerCellNetwork generatorNetwork = PowerCellNetwork.getChannels(world);
            PowerCellNetwork.Network network = generatorNetwork.getChannel(id);
            long totInserted = 0;
            long totExtracted = 0;
            int rfPerTick = 0;
            float costFactor = 0;

            if (te instanceof PowerCellTileEntity) {
                PowerCellTileEntity powerCellTileEntity = (PowerCellTileEntity) te;
                totInserted = powerCellTileEntity.getTotalInserted();
                totExtracted = powerCellTileEntity.getTotalExtracted();
                rfPerTick = powerCellTileEntity.getRfPerTickPerSide();
                costFactor = powerCellTileEntity.getCostFactor();
            }

            return Optional.of(new PowerCellInfoPacketClient(network.getEnergy(), network.getBlockCount(),
                    network.getSimpleBlockCount(),
                    network.getAdvancedBlockCount(),
                    totInserted, totExtracted, rfPerTick, costFactor));
        }
    }
}
