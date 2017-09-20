package mcjty.rftools.shapes;

import gnu.trove.set.hash.TLongHashSet;
import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketRequestShapeData implements IMessage {
    private ItemStack card;

    @Override
    public void fromBytes(ByteBuf buf) {
        card = NetworkTools.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeItemStack(buf, card);
    }

    public PacketRequestShapeData() {
    }

    public PacketRequestShapeData(ItemStack card) {
        this.card = card;
    }

    public static class Handler implements IMessageHandler<PacketRequestShapeData, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestShapeData message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestShapeData message, MessageContext ctx) {
            Shape shape = ShapeCardItem.getShape(message.card);
            Map<Long, IBlockState> stateMap = new HashMap<Long, IBlockState>();
            TLongHashSet positions = ShapeCardItem.getPositions(message.card, shape, false, new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), stateMap);
            RFToolsMessages.INSTANCE.sendTo(new PacketReturnShapeData(positions, stateMap), ctx.getServerHandler().player);
        }
    }

}
