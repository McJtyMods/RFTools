package mcjty.rftools.blocks.shaper;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketProjectorClientNotification implements IMessage {
    private BlockPos pos;
    private float verticalOffset;
    private float scale;
    private float angle;
    private boolean autoRotate;
    private boolean projecting;
    private boolean scanline;
    private boolean sound;
    private int counter;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        verticalOffset = buf.readFloat();
        scale = buf.readFloat();
        angle = buf.readFloat();
        autoRotate = buf.readBoolean();
        projecting = buf.readBoolean();
        scanline = buf.readBoolean();
        sound = buf.readBoolean();
        counter = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeFloat(verticalOffset);
        buf.writeFloat(scale);
        buf.writeFloat(angle);
        buf.writeBoolean(autoRotate);
        buf.writeBoolean(projecting);
        buf.writeBoolean(scanline);
        buf.writeBoolean(sound);
        buf.writeInt(counter);
    }

    public float getVerticalOffset() {
        return verticalOffset;
    }

    public float getScale() {
        return scale;
    }

    public float getAngle() {
        return angle;
    }

    public boolean isAutoRotate() {
        return autoRotate;
    }

    public boolean isProjecting() {
        return projecting;
    }

    public boolean isScanline() {
        return scanline;
    }

    public int getCounter() {
        return counter;
    }

    public boolean isSound() {
        return sound;
    }

    public PacketProjectorClientNotification() {
    }

    public PacketProjectorClientNotification(ProjectorTileEntity tileEntity) {
        pos = tileEntity.getPos();
        verticalOffset = tileEntity.getVerticalOffset();
        scale = tileEntity.getScale();
        angle = tileEntity.getAngle();
        autoRotate = tileEntity.isAutoRotate();
        projecting = tileEntity.isProjecting();
        scanline = tileEntity.isScanline();
        sound = tileEntity.isSound();
        counter = tileEntity.getCounter();
    }

    public static class Handler implements IMessageHandler<PacketProjectorClientNotification, IMessage> {
        @Override
        public IMessage onMessage(PacketProjectorClientNotification message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message));
            return null;
        }

        private void handle(PacketProjectorClientNotification message) {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(message.pos);
            if (te instanceof ProjectorTileEntity) {
                ((ProjectorTileEntity) te).updateFromServer(message);;
            }
        }
    }
}