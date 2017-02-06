package mcjty.rftools.blocks.monitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    public static class Handler implements IMessageHandler<PacketContentsMonitor, IMessage> {
        @Override
        public IMessage onMessage(PacketContentsMonitor message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketContentsMonitor message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().player.getEntityWorld().getTileEntity(message.pos);
            if (te instanceof RFMonitorBlockTileEntity) {
                RFMonitorBlockTileEntity monitorBlockTileEntity = (RFMonitorBlockTileEntity) te;
                if (message.monitor != null) {
                    monitorBlockTileEntity.setMonitor(message.monitor);
                }
                if (message.alarmLevel != -1) {
                    monitorBlockTileEntity.setAlarm(message.alarmMode, message.alarmLevel);
                }
            } else if (te instanceof LiquidMonitorBlockTileEntity) {
                LiquidMonitorBlockTileEntity liquidMonitorBlockTileEntity = (LiquidMonitorBlockTileEntity) te;
                if (message.monitor != null) {
                    liquidMonitorBlockTileEntity.setMonitor(message.monitor);
                }
                if (message.alarmLevel != -1) {
                    liquidMonitorBlockTileEntity.setAlarm(message.alarmMode, message.alarmLevel);
                }
            } else {
                Logging.log("TileEntity is not a RFMonitorBlockTileEntity!");
            }
        }

    }
}
