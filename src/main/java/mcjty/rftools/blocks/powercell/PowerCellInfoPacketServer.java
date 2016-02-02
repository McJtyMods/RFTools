package mcjty.rftools.blocks.powercell;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Optional;

public class PowerCellInfoPacketServer implements InfoPacketServer {

    private int id;

    public PowerCellInfoPacketServer() {
    }

    public PowerCellInfoPacketServer(int id) {
        this.id = id;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
    }

    @Override
    public Optional<InfoPacketClient> onMessageServer(EntityPlayerMP player) {
        World world = player.worldObj;

        PowerCellNetwork generatorNetwork = PowerCellNetwork.getChannels(world);
        PowerCellNetwork.Network network = generatorNetwork.getChannel(id);

        return Optional.of(new PowerCellInfoPacketClient(network.getEnergy(), network.getBlockCount()));
    }
}
