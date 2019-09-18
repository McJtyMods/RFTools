package mcjty.rftools.blocks.screens.network;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Map;
import java.util.function.Supplier;

public class PacketGetScreenData {
    private String modid;
    private GlobalCoordinate pos;
    private long millis;

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writeString(buf, modid);
        NetworkTools.writePos(buf, pos.getCoordinate());
        buf.writeInt(pos.getDimension().getId());
        buf.writeLong(millis);
    }

    public PacketGetScreenData() {
    }

    public PacketGetScreenData(PacketBuffer buf) {
        modid = NetworkTools.readString(buf);
        pos = new GlobalCoordinate(NetworkTools.readPos(buf), DimensionType.getById(buf.readInt()));
        millis = buf.readLong();
    }

    public PacketGetScreenData(String modid, GlobalCoordinate pos, long millis) {
        this.modid = modid;
        this.pos = pos;
        this.millis = millis;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = ctx.getSender().getEntityWorld();
            if (!pos.getDimension().equals(world.getDimension().getType())) {
                return;
            }
            TileEntity te = world.getTileEntity(pos.getCoordinate());
            if(!(te instanceof ScreenTileEntity)) {
                Logging.logError("PacketGetScreenData: TileEntity is not a SimpleScreenTileEntity!");
                return;
            }
            Map<Integer, IModuleData> screenData = ((ScreenTileEntity) te).getScreenData(millis);

            SimpleChannel wrapper = PacketHandler.modNetworking.get(modid);
            PacketReturnScreenData msg = new PacketReturnScreenData(pos, screenData);
            wrapper.sendTo(msg, ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }

}