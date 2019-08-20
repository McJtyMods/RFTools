package mcjty.rftools.blocks.shaper;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketProjectorClientNotification {
    private BlockPos pos;
    private float verticalOffset;
    private float scale;
    private float angle;
    private boolean autoRotate;
    private boolean projecting;
    private boolean scanline;
    private boolean sound;
    private boolean grayscale;
    private int counter;

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeFloat(verticalOffset);
        buf.writeFloat(scale);
        buf.writeFloat(angle);
        buf.writeBoolean(autoRotate);
        buf.writeBoolean(projecting);
        buf.writeBoolean(scanline);
        buf.writeBoolean(sound);
        buf.writeBoolean(grayscale);
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

    public boolean isGrayscale() {
        return grayscale;
    }

    public PacketProjectorClientNotification() {
    }

    public PacketProjectorClientNotification(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
        verticalOffset = buf.readFloat();
        scale = buf.readFloat();
        angle = buf.readFloat();
        autoRotate = buf.readBoolean();
        projecting = buf.readBoolean();
        scanline = buf.readBoolean();
        sound = buf.readBoolean();
        grayscale = buf.readBoolean();
        counter = buf.readInt();
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
        grayscale = tileEntity.isGrayscale();
        counter = tileEntity.getCounter();
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(pos);
            if (te instanceof ProjectorTileEntity) {
                ((ProjectorTileEntity) te).updateFromServer(this);
            }
        });
        ctx.setPacketHandled(true);
    }
}