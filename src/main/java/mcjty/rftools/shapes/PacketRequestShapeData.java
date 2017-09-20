package mcjty.rftools.shapes;

import gnu.trove.iterator.TLongIterator;
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
    private boolean count;

    @Override
    public void fromBytes(ByteBuf buf) {
        card = NetworkTools.readItemStack(buf);
        count = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeItemStack(buf, card);
        buf.writeBoolean(count);
    }

    public PacketRequestShapeData() {
    }

    public PacketRequestShapeData(ItemStack card, boolean count) {
        this.card = card;
        this.count = count;
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
            boolean solid = ShapeCardItem.isSolid(message.card);
            TLongHashSet positionsFull = ShapeCardItem.getPositions(message.card, shape, solid, new BlockPos(0, 0, 0), new BlockPos(0, 0, 0), stateMap);
            int cnt = positionsFull.size();
            // Remove all blocks that are fully enclosed (not visible) but only if we are solid. If not solid this is not needed
            TLongHashSet positions;
            if (solid) {
                positions = new TLongHashSet();
                TLongIterator iterator = positionsFull.iterator();
                while (iterator.hasNext()) {
                    long pos = iterator.next();
                    if (!isPositionEnclosed(positionsFull, BlockPos.fromLong(pos))) {
                        positions.add(pos);
                    }
                }
            } else {
                positions = positionsFull;
            }

//            if (message.count) {
//                cnt = ShapeCardItem.countBlocks(message.card, shape, ShapeCardItem.isSolid(message.card), ShapeCardItem.getDimension(message.card));
//            }
            RFToolsMessages.INSTANCE.sendTo(new PacketReturnShapeData(positions, stateMap, cnt), ctx.getServerHandler().player);
        }
    }

    static boolean isPositionEnclosed(TLongHashSet positions, BlockPos coordinate) {
        return positions.contains(coordinate.up().toLong()) &&
                positions.contains(coordinate.down().toLong()) &&
                positions.contains(coordinate.east().toLong()) &&
                positions.contains(coordinate.west().toLong()) &&
                positions.contains(coordinate.south().toLong()) &&
                positions.contains(coordinate.north().toLong());
    }

}
