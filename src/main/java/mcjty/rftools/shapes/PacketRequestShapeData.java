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

            System.out.println("CALC START : 1");

            Shape shape = ShapeCardItem.getShape(message.card);
            boolean solid = ShapeCardItem.isSolid(message.card);
            BlockPos dimension = ShapeCardItem.getDimension(message.card);

            if (dimension.getX() > 100 || dimension.getY() > 100 || dimension.getZ() > 100) {
                System.out.println("Sorry, no preview");
                return;
            }

            System.out.println("dimension = " + dimension);
            Map<Long, IBlockState> positionsFull = ShapeCardItem.getPositions(message.card, shape, solid, new BlockPos(0, 0, 0), new BlockPos(0, 0, 0));
            System.out.println("CALC START : 2");
            int cnt = positionsFull.size();
            // Remove all blocks that are fully enclosed (not visible) but only if we are solid. If not solid this is not needed
            Map<Long, IBlockState> positions;
            if (solid) {
                positions = new HashMap<>();
                for (Map.Entry<Long, IBlockState> entry : positionsFull.entrySet()) {
                    long pos = entry.getKey();
                    if (!isPositionEnclosed(positionsFull, BlockPos.fromLong(pos))) {
                        positions.put(pos, entry.getValue());
                    }
                }
            } else {
                positions = positionsFull;
            }

            System.out.println("CALC STOP");

            RFToolsMessages.INSTANCE.sendTo(new PacketReturnShapeData(positions, cnt), ctx.getServerHandler().player);
        }
    }

    static boolean isPositionEnclosed(Map<Long, IBlockState> positions, BlockPos coordinate) {
        return positions.containsKey(coordinate.up().toLong()) &&
                positions.containsKey(coordinate.down().toLong()) &&
                positions.containsKey(coordinate.east().toLong()) &&
                positions.containsKey(coordinate.west().toLong()) &&
                positions.containsKey(coordinate.south().toLong()) &&
                positions.containsKey(coordinate.north().toLong());
    }

}
