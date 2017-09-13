package mcjty.rftools.blocks.shaper;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tools.EntityTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.items.builder.ShapeOperation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSendShaperData implements IMessage {
    private BlockPos pos;
    private ShapeOperation operations[];

    @Override
    public void fromBytes(ByteBuf buf) {
        int s = buf.readInt();
        operations = new ShapeOperation[s];
        for (int i = 0 ; i < s ; i++) {
            String code = NetworkTools.readString(buf);
            operations[i] = ShapeOperation.getByName(code);
        }
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(operations.length);
        for (ShapeOperation operation : operations) {
            NetworkTools.writeString(buf, operation.getCode());
        }
        NetworkTools.writePos(buf, pos);
    }

    public PacketSendShaperData() {
    }

    public PacketSendShaperData(BlockPos pos, ShapeOperation[] operations) {
        this.pos = pos;
        this.operations = operations;
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
                ((ShaperTileEntity) te).setOperations(message.operations);
            }
        }

    }

}
