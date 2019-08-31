package mcjty.rftools.shapes;

import mcjty.lib.network.NetworkTools;
import mcjty.rftools.blocks.shaper.ComposerTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public class PacketSendComposerData {
    private BlockPos pos;
    private ShapeModifier modifiers[];

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(modifiers.length);
        for (ShapeModifier modifier : modifiers) {
            NetworkTools.writeString(buf, modifier.getOperation().getCode());
            buf.writeBoolean(modifier.isFlipY());
            NetworkTools.writeString(buf, modifier.getRotation().getCode());
        }
        NetworkTools.writePos(buf, pos);
    }

    public PacketSendComposerData() {
    }

    public PacketSendComposerData(PacketBuffer buf) {
        int s = buf.readInt();
        modifiers = new ShapeModifier[s];
        for (int i = 0 ; i < s ; i++) {
            String code = NetworkTools.readString(buf);
            ShapeOperation op = ShapeOperation.getByName(code);
            boolean flip = buf.readBoolean();
            code = NetworkTools.readString(buf);
            ShapeRotation rot = ShapeRotation.getByName(code);
            modifiers[i] = new ShapeModifier(op, flip, rot);
        }
        pos = NetworkTools.readPos(buf);
    }

    public PacketSendComposerData(BlockPos pos, ShapeModifier[] modifiers) {
        this.pos = pos;
        this.modifiers = modifiers;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if (te instanceof ComposerTileEntity) {
                ((ComposerTileEntity) te).setModifiers(modifiers);
            }
        });
        ctx.setPacketHandled(true);
    }
}
