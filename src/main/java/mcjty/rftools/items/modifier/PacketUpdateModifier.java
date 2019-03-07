package mcjty.rftools.items.modifier;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketUpdateModifier implements IMessage {
    private ModifierCommand cmd = ModifierCommand.ADD;
    private int index;
    private ModifierFilterType type;
    private ModifierFilterOperation op;

    @Override
    public void fromBytes(ByteBuf buf) {
        cmd = ModifierCommand.values()[buf.readByte()];
        index = buf.readInt();
        type = ModifierFilterType.values()[buf.readByte()];
        op = ModifierFilterOperation.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(cmd == null ? 0 : cmd.ordinal());
        buf.writeInt(index);
        buf.writeByte(type == null ? 0 : type.ordinal());
        buf.writeByte(op == null ? 0 : op.ordinal());
    }

    public PacketUpdateModifier() {
    }

    public PacketUpdateModifier(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketUpdateModifier(ModifierCommand cmd, int index, ModifierFilterType type, ModifierFilterOperation op) {
        this.cmd = cmd;
        this.index = index;
        this.type = type;
        this.op = op;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            EntityPlayer player = ctx.getSender();
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (!heldItem.isEmpty() && heldItem.getItem() == ModItems.modifierItem) {
                ModifierItem.performCommand(player, heldItem, cmd, index, type, op);
            }
        });
        ctx.setPacketHandled(true);
    }
}
