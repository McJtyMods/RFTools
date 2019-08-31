package mcjty.rftools.items.modifier;

import mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateModifier {
    private ModifierCommand cmd = ModifierCommand.ADD;
    private int index;
    private ModifierFilterType type;
    private ModifierFilterOperation op;

    public void toBytes(PacketBuffer buf) {
        buf.writeByte(cmd == null ? 0 : cmd.ordinal());
        buf.writeInt(index);
        buf.writeByte(type == null ? 0 : type.ordinal());
        buf.writeByte(op == null ? 0 : op.ordinal());
    }

    public PacketUpdateModifier() {
    }

    public PacketUpdateModifier(PacketBuffer buf) {
        cmd = ModifierCommand.values()[buf.readByte()];
        index = buf.readInt();
        type = ModifierFilterType.values()[buf.readByte()];
        op = ModifierFilterOperation.values()[buf.readByte()];
    }

    public PacketUpdateModifier(ModifierCommand cmd, int index, ModifierFilterType type, ModifierFilterOperation op) {
        this.cmd = cmd;
        this.index = index;
        this.type = type;
        this.op = op;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            PlayerEntity player = ctx.getSender();
            ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
            if (!heldItem.isEmpty() && heldItem.getItem() == ModItems.modifierItem) {
                ModifierItem.performCommand(player, heldItem, cmd, index, type, op);
            }
        });
        ctx.setPacketHandled(true);
    }
}
