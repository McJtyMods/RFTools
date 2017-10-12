package mcjty.rftools.items.modifier;

import io.netty.buffer.ByteBuf;
import mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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

    public PacketUpdateModifier(ModifierCommand cmd, int index, ModifierFilterType type, ModifierFilterOperation op) {
        this.cmd = cmd;
        this.index = index;
    }

    public static class Handler implements IMessageHandler<PacketUpdateModifier, IMessage> {
        @Override
        public IMessage onMessage(PacketUpdateModifier message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketUpdateModifier message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (!heldItem.isEmpty() && heldItem.getItem() == ModItems.modifierItem) {
                ModifierItem.performCommand(player, heldItem, message.cmd, message.index, message.type, message.op);
            }
        }
    }
}
