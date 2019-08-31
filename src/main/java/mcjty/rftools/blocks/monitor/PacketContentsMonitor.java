package mcjty.rftools.blocks.monitor;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketContentsMonitor {
    private BlockPos pos;
    private BlockPos monitor;

    private int alarmLevel;
    private RFMonitorMode alarmMode;

    public PacketContentsMonitor() {
        monitor = null;
        alarmLevel = -1;
        alarmMode = RFMonitorMode.MODE_OFF;
    }

    public PacketContentsMonitor(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
        boolean r = buf.readBoolean();
        if (r) {
            monitor = NetworkTools.readPos(buf);
        }
        alarmLevel = buf.readByte();
        alarmMode = RFMonitorMode.getModeFromIndex(buf.readByte());
    }

    public PacketContentsMonitor(BlockPos pos, BlockPos monitor) {
        this();
        this.pos = pos;
        this.monitor = monitor;
    }

    public PacketContentsMonitor(BlockPos pos, RFMonitorMode alarmMode, int alarmLevel) {
        this();
        this.pos = pos;
        this.alarmLevel = alarmLevel;
        this.alarmMode = alarmMode;
    }

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writePos(buf, pos);
        if (monitor == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            NetworkTools.writePos(buf, monitor);
        }
        buf.writeByte(alarmLevel);
        buf.writeByte(alarmMode.getIndex());
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if (te instanceof RFMonitorBlockTileEntity) {
                RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
                if (monitor != null) {
                    monitorBlockTileEntity.setMonitor(monitor);
                }
                if (alarmLevel != -1) {
                    monitorBlockTileEntity.setAlarm(alarmMode, alarmLevel);
                }
            } else if (te instanceof LiquidMonitorBlockTileEntity) {
                LiquidMonitorBlockTileEntity liquidMonitorBlockTileEntity = (LiquidMonitorBlockTileEntity) te;
                if (monitor != null) {
                    liquidMonitorBlockTileEntity.setMonitor(monitor);
                }
                if (alarmLevel != -1) {
                    liquidMonitorBlockTileEntity.setAlarm(alarmMode, alarmLevel);
                }
            } else {
                Logging.log("TileEntity is not a RFMonitorBlockTileEntity!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
