package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketClearTarget implements IMessage {
    private int index;

    @Override
    public void fromBytes(ByteBuf buf) {
        index = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(index);
    }

    public PacketClearTarget() {
    }

    public PacketClearTarget(int target) {
        this.index = target;
    }

    public static class Handler implements IMessageHandler<PacketClearTarget, IMessage> {
        @Override
        public IMessage onMessage(PacketClearTarget message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketClearTarget message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (ItemStackTools.isEmpty(heldItem)) {
                return;
            }
            NBTTagCompound tagCompound = heldItem.getTagCompound();
            if (tagCompound == null) {
                return;
            }
            if (tagCompound.hasKey("target"+message.index)) {
                int id = tagCompound.getInteger("target"+message.index);
                if (tagCompound.hasKey("target") && tagCompound.getInteger("target") == id) {
                    tagCompound.removeTag("target");
                }
                tagCompound.removeTag("target"+message.index);
            }
        }
    }

}
