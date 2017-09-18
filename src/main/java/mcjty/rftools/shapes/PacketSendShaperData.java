package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.blocks.shaper.ShaperTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSendShaperData implements IMessage {
    private BlockPos pos;
    private ShapeModifier modifiers[];

    @Override
    public void fromBytes(ByteBuf buf) {
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

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(modifiers.length);
        for (ShapeModifier modifier : modifiers) {
            NetworkTools.writeString(buf, modifier.getOperation().getCode());
            buf.writeBoolean(modifier.isFlipY());
            NetworkTools.writeString(buf, modifier.getRotation().getCode());
        }
        NetworkTools.writePos(buf, pos);
    }

    public PacketSendShaperData() {
    }

    public PacketSendShaperData(BlockPos pos, ShapeModifier[] modifiers) {
        this.pos = pos;
        this.modifiers = modifiers;
    }

    public static class Handler implements IMessageHandler<PacketSendShaperData, IMessage> {
        @Override
        public IMessage onMessage(PacketSendShaperData message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSendShaperData message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().player.getEntityWorld().getTileEntity(message.pos);
            if (te instanceof ShaperTileEntity) {
                ((ShaperTileEntity) te).setModifiers(message.modifiers);
            }
        }

    }

}
