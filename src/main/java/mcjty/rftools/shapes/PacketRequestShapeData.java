package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.items.builder.ShapeCardItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestShapeData implements IMessage {
    private ItemStack card;
    private ShapeID id;

    @Override
    public void fromBytes(ByteBuf buf) {
        card = NetworkTools.readItemStack(buf);
        id = new ShapeID(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeItemStack(buf, card);
        id.toBytes(buf);
    }

    public PacketRequestShapeData() {
    }

    public PacketRequestShapeData(ItemStack card, ShapeID id) {
        this.card = card;
        this.id = id;
    }

    public static class Handler implements IMessageHandler<PacketRequestShapeData, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestShapeData message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestShapeData message, MessageContext ctx) {

            Shape shape = ShapeCardItem.getShape(message.card);
            boolean solid = ShapeCardItem.isSolid(message.card);
            BlockPos dimension = ShapeCardItem.getDimension(message.card);

//            if (dimension.getX()*dimension.getY()*dimension.getZ() > (256 * 256 * 256)) {
//                RFToolsMessages.INSTANCE.sendTo(new PacketReturnShapeData(message.id, null, null, dimension, 0, "Too large for preview!"), ctx.getServerHandler().player);
//                return;
//            }

            BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));
            int dy = clamped.getY();
            ItemStack card = message.card.copy();

            IFormula formula = shape.getFormulaFactory().createFormula();
            formula = formula.correctFormula(solid);
            formula.setup(new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), message.card.getTagCompound());

            for (int y = 0 ; y < dy ; y++) {
                ShapeDataManager.pushWork(message.id, card, y, formula, ctx.getServerHandler().player);
            }


//            RLE positions = new RLE();
//            StatePalette statePalette = new StatePalette();
//            int cnt = ShapeCardItem.getRenderPositions(message.card, shape, solid, positions, statePalette);
//
//            RFToolsMessages.INSTANCE.sendTo(new PacketReturnShapeData(message.id, positions, statePalette, dimension, cnt, ""), ctx.getServerHandler().player);
        }
    }


}
