package mcjty.rftools.blocks.monitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.lib.varia.Logging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketContentsMonitor implements IMessage {
    private BlockPos pos;
    private BlockPos monitor;

    private int alarmLevel;
    private RFMonitorMode alarmMode;

    public PacketContentsMonitor() {
        monitor = null;
        alarmLevel = -1;
        alarmMode = RFMonitorMode.MODE_OFF;
    }

    public PacketContentsMonitor(ByteBuf buf) {
        fromBytes(buf);
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

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        boolean r = buf.readBoolean();
        if (r) {
            monitor = NetworkTools.readPos(buf);
        }
        alarmLevel = buf.readByte();
        alarmMode = RFMonitorMode.getModeFromIndex(buf.readByte());
    }

    @Override
    public void toBytes(ByteBuf buf) {
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

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
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
