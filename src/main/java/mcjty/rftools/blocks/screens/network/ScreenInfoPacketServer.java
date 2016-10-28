package mcjty.rftools.blocks.screens.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import mcjty.lib.network.clientinfo.InfoPacketServer;
import mcjty.rftools.api.screens.IScreenModule;
import mcjty.rftools.api.screens.ITooltipInfo;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.Optional;

public class ScreenInfoPacketServer implements InfoPacketServer {

    private int dimension;
    private BlockPos pos;

    public ScreenInfoPacketServer() {
    }

    public ScreenInfoPacketServer(int dimension, BlockPos pos) {
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
        String[] info = new String[0];
        int cnt = -1;
        if (world != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ScreenTileEntity) {
                ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
                IScreenModule module = screenTileEntity.getHoveringModule();
                if (module instanceof ITooltipInfo) {
                    info = ((ITooltipInfo) module).getInfo(world, screenTileEntity.getHoveringX(), screenTileEntity.getHoveringY(), player);
                }
            }
        }
        return Optional.of(new ScreenInfoPacketClient(info));
    }
}
