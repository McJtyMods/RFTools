package mcjty.rftools.shapes;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.items.builder.ShapeCardItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

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

    public PacketRequestShapeData(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketRequestShapeData(ItemStack card, ShapeID id) {
        this.card = card;
        this.id = id;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            Shape shape = ShapeCardItem.getShape(card);
            boolean solid = ShapeCardItem.isSolid(card);
            BlockPos dimension = ShapeCardItem.getDimension(card);

            BlockPos clamped = new BlockPos(Math.min(dimension.getX(), 512), Math.min(dimension.getY(), 256), Math.min(dimension.getZ(), 512));
            int dy = clamped.getY();
            card = card.copy();

            IFormula formula = shape.getFormulaFactory().get();
            formula = formula.correctFormula(solid);
            formula.setup(new BlockPos(0, 0, 0), clamped, new BlockPos(0, 0, 0), card.getTagCompound());

            for (int y = 0 ; y < dy ; y++) {
                ShapeDataManagerServer.pushWork(id, card, y, formula, ctx.getSender());
            }
        });
        ctx.setPacketHandled(true);
    }
}
